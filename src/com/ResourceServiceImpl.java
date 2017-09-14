@Service("resourceService")
public class ResourceServiceImpl implements ResourceService {

	@Autowired
	private DebugLogService debugLogService;
	@Autowired
	private ResourceQueryService resourceQueryService;
	@Autowired
	private ResourceMapper resourceMapper;
	@Autowired
	private ResourceTransactionMapper resourceTransactionMapper;
	@Autowired
	private ResourceSetupService resourceSetupService;
	@Autowired
	private ITSMTicketService itsmTicketService;
	@Autowired
	private NotificationManagerService notificationManagerService;
	@Autowired
	private OrganizationITMSService organizationITMSService;
	@Autowired
	private UserITMSMapper userITMSMapper;
	@Autowired
	private EmailSenderService emailSenderService;
	@Autowired
	private UserITMSService userITMSService;
	@Autowired
	private DashboardApprovalTaskMapper dashboardApprovalTaskMapper;

	@Autowired
	private NotificationMessageDAO notificationMessageDAO;
	@Autowired
	private ITMSNotificationTemplateMapper notificationTemplateMapper;





	@Transactional
	@Override
	public ServiceResult<HashMap<String, Object>> saveRegisterOutsource(OutEmployee outEmployee,
			EmployeePersonalInformation empPersonalInfo, List<EmployeeLob> relatedLob,
			List<EmployeeFunction> employeeFunctions, List<EmployeeCertification> employeeCertifications,
			List<DocumentITMS> documentCertificates, String flagCreateSecurityTicket, Long executorId,
			Long responsibilityId) {

		ServiceResult<HashMap<String, Object>> result = new ServiceResult<>();
		ServiceResult<ITSMTicket> securityTicket = new ServiceResult<>();

		try {

			HashMap<String, Object> map = new HashMap<>();
			OutEmployee outEmployeeNew = new OutEmployee(outEmployee);

			List<OutEmployee> regCheckDuplicated = new ArrayList<>();

			if (null != outEmployeeNew.getNpo() && !outEmployeeNew.getNpo().isEmpty()
					&& outEmployeeNew.getNpo().trim().length() != 0) {
				/** check duplicate npo */
				regCheckDuplicated = resourceTransactionMapper
						.getOutsourceEmployeeByNPO(outEmployeeNew.getNpo().trim());

			}

			if (regCheckDuplicated.size() != 0) {
				result.addErrorMessage("NPO already exist");
				result.setSuccess(false);
				return result;
			}

			/** NPO must not contain numbers only
			 *  will checked if npo contain values */
			Boolean isNotContainNumericOnly = Boolean.FALSE;
			if (null != outEmployeeNew.getNpo() && !outEmployeeNew.getNpo().isEmpty() && outEmployeeNew.getNpo().trim().length() != 0) {
				boolean isNumeric = CheckNpoIsNumerics(outEmployeeNew.getNpo());
				if (isNumeric == true) {
					isNotContainNumericOnly = Boolean.TRUE;
				} else {
					isNotContainNumericOnly = Boolean.FALSE;
				}
			}

			if (isNotContainNumericOnly == Boolean.TRUE) {
				result.addErrorMessage("NPO must not contain numbers only");
				result.setSuccess(false);
				return result;
			}

			int rowAffected = 0;
			/** INSERT OUTSOURCE EMPLOYEE **/
			rowAffected = resourceTransactionMapper.insertOutEmployee(outEmployeeNew, executorId);

			/** Jika uploaded Photo */
			String fileName = null;
			if (outEmployeeNew.getEmployeePhoto() != null) {
				/** set path photo */
				String path = PathFileEnum.REGISTER_PHOTO_FILE.getName();
				String extensionFile = outEmployeeNew.getExtensionPhoto();
				fileName = path + String.valueOf(outEmployeeNew.getOutEmployeeId()).replace("-", "Out") + extensionFile;

				/** update path foto */
				outEmployeeNew.setEmployeePhoto(fileName);
				rowAffected = resourceTransactionMapper.updateRegisterOutsourceImageName(outEmployeeNew);
			}

			

			/** INSERT PERSONAL INFORMATION **/
			EmployeePersonalInformation employeePersonalInformation = new EmployeePersonalInformation(empPersonalInfo);
			employeePersonalInformation.setOutEmployeeId(outEmployeeNew.getOutEmployeeId());

			rowAffected = resourceTransactionMapper.insertEmployeePersonalInformation(employeePersonalInformation,
					executorId);

			/** INSERT RELATED LOB */
			if (relatedLob != null) {
				List<EmployeeLob> lstEmployeeLobs = new ArrayList<>();
				lstEmployeeLobs.addAll(relatedLob);
				for (EmployeeLob employeeLob : lstEmployeeLobs) {
					employeeLob.setEmployeeId(outEmployeeNew.getOutEmployeeId());
					rowAffected = resourceTransactionMapper.insertRelatedLOB(employeeLob, executorId);
				}
			}

			/** INSERT EMPLOYEE FUNCTION */
			if (employeeFunctions != null) {
				List<EmployeeFunction> lstEmployeeFunction = new ArrayList<>();
				lstEmployeeFunction.addAll(employeeFunctions);
				for (EmployeeFunction employeeFunction : lstEmployeeFunction) {
					employeeFunction.setEmployeeId(outEmployeeNew.getOutEmployeeId());
					rowAffected = resourceTransactionMapper.insertEmployeeFunction(employeeFunction, executorId);
				}
			}

			/** INSERT CERTIFICATES */
			if (employeeCertifications != null) {
				List<EmployeeCertification> lstEmployeeCertification = new ArrayList<>();
				lstEmployeeCertification.addAll(employeeCertifications);
				for (EmployeeCertification employeeCertification : lstEmployeeCertification) {
					employeeCertification.setEmployeeId(outEmployeeNew.getOutEmployeeId());
					rowAffected = resourceTransactionMapper.insertEmpOutCertifications(employeeCertification,
							executorId);
				}
			}

			/** INSERT DOCUMENT CERTIFICATES */
			if (documentCertificates != null) {
				List<DocumentITMS> documentCertificate = new ArrayList<>();
				List<EmployeeCertificates> lstEmployeeCertificates = new ArrayList<>();
				for (DocumentITMS documentITMS : documentCertificates) {

					documentITMS.setDocumentName(documentITMS.getDocumentName());
					documentITMS.setDocumentPath(documentITMS.getDocumentPath());

					rowAffected = resourceTransactionMapper.insertDumpDocumentFile(documentITMS, executorId);

					EmployeeCertificates empCertificate = new EmployeeCertificates();
					empCertificate.setOutEmployeeId(outEmployeeNew.getOutEmployeeId());
					empCertificate.setDocumentId(documentITMS.getDocumentId());

					lstEmployeeCertificates.add(empCertificate);
				}

				/** insert certificates file upload */
				for (EmployeeCertificates employeeCertificates : lstEmployeeCertificates) {
					rowAffected = resourceTransactionMapper.insertDocumentCertificateFiles(employeeCertificates,
							executorId);
				}

			}

			/**
			 * send notification dashboard, create ticket atau tidak tetap mengirim notifikasi
			 */
			ServiceResult<ITMSNotificationTemplate> svcTemplateResult = notificationManagerService
					.getNotificationTemplate("REGISTER_OUT", "REGISTER_OUT", executorId);
			if (!svcTemplateResult.isSuccess()) {
				throw new CustomException("Error when get template get template, please check template notification  ");
			}

			if (svcTemplateResult.getResult() == null) {
				throw new CustomException("Null value when get template get template, please check template notification  ");
			}

			/** get template notification from database */
			ITMSNotificationTemplate template = svcTemplateResult.getResult();

			UserITMS usersToBeNotified = userITMSMapper.getUserByPersonId(outEmployeeNew.getSpv1PersonId());
			String urlTarget = ITMSPages.REVIEW_REGISTER_OUTSOURCE_EMPLOYEE.getUrl() + "?personId="+ outEmployeeNew.getOutEmployeeId() + "&caller=NOTIFICATION";


			/** get template for replace */
			HashMap<String, Object> mapTemplate = templNotifycation(outEmployeeNew, urlTarget, usersToBeNotified);

			String subject = Utils.replaceTemplate(template.getTemplateSubject(), mapTemplate);
			String message = Utils.replaceTemplate(template.getTemplateContent(), mapTemplate);

			notificationManagerService.sendNotification(subject, message, urlTarget, MessageType.FYI_MESSAGE,usersToBeNotified, executorId);

			if (outEmployeeNew.getSpv2PersonId() != null) {
				UserITMS usersToBeNotified2 = userITMSMapper.getUserByPersonId(outEmployeeNew.getSpv2PersonId());
				HashMap<String, Object> mapTemplate2 = templNotifycation(outEmployeeNew, urlTarget, usersToBeNotified2);

				String subject2 = Utils.replaceTemplate(template.getTemplateSubject(), mapTemplate2);
				String message2 = Utils.replaceTemplate(template.getTemplateContent(), mapTemplate2);

				UserITMS usersToBeNotifiedSp2 = userITMSMapper.getUserByPersonId(outEmployeeNew.getSpv2PersonId());
				notificationManagerService.sendNotification(subject2, message2, urlTarget, MessageType.FYI_MESSAGE,
						usersToBeNotifiedSp2, executorId);

			}

			map.put("isOutEmployeeSuccess", true);
			map.put("outEmployee", outEmployeeNew);

			/**
			 * set default isTicketSuccess  
			 * Jika generate security ticket sukes maka isTicketSuccess di isi @true
			 * jika tidak melakukan generate security maka isTicketSuccess di isi  @false
			 */
			map.put("isTicketSuccess", false);
			map.put("ticket", new ITSMTicket());

			if (flagCreateSecurityTicket.equals("Y")) {
				// create security ticket
				securityTicket = createGenerateSecurityTicket(outEmployeeNew, executorId, responsibilityId);
				if (!securityTicket.isSuccess()) {
					map.put("isTicketSuccess", false);
					securityTicket.setSuccess(false);
					securityTicket.addErrorMessage(securityTicket.getFirstErrorMessage());
				} else {
					map.put("isTicketSuccess", true);
					map.put("ticket", securityTicket.getResult());
					securityTicket.setSuccess(true);
					securityTicket.setResult(securityTicket.getResult());
					ITSMTicket scTicket = securityTicket.getResult();
					/**update security ticket id  */
					outEmployeeNew.setRegSecurityTicketId(scTicket.getTicketId());
					rowAffected = resourceTransactionMapper.updateRegisterOutsourceById(outEmployeeNew);
				}
			}
			

			if (flagCreateSecurityTicket.equals("Y")) {
				if (!securityTicket.isSuccess()) {
					result.setSuccess(true);
					result.addErrorMessage("Data has been save and system failed to generate security ticket: "+ securityTicket.getFirstErrorMessage());
				} else {
					result.addErrorMessage("Data has been save and security ticket has been sent to ITSM");
				}
			} else {
				result.addErrorMessage("Data has been save");
			}
			result.setSuccess(true);
			result.setResult(map);
		} catch (CustomException e) {
			e.printStackTrace();
			result.setSuccess(false);
			result.addErrorMessage(e.getMessage());
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			debugLogService.writeErrorLog(e, Utils.getSessionUserId(), getClass());
		} catch (Exception e) {
			e.printStackTrace();
			result.setSuccess(false);
			result.addErrorMessage("unhadler.error");
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			debugLogService.writeErrorLog(e, Utils.getSessionUserId(), getClass());
		}
		return result;
	}


	// ===========================================================
	// Template Notification
	// ===========================================================
	public HashMap<String, Object> templNotifycation(OutEmployee outEmployeeNew, String urlTarget,
			UserITMS usersToBeNotified) throws CustomException {
		OrganizationITMS organization = organizationITMSService.getOrganizationById(outEmployeeNew.getOrganizationId());
		if (null == organization) {
			throw new CustomException("null value in get organization name on send noticitaion");
		}

		UserITMS userOne = userITMSMapper.getUserByPersonId(outEmployeeNew.getSpv1PersonId());
		if (null == userOne) {
			throw new CustomException("null value in get username on send noticitaion");
		}
		UserITMS userTwo = new UserITMS();

		if (outEmployeeNew.getSpv2PersonId() != null) {
			userTwo = userITMSMapper.getUserByPersonId(outEmployeeNew.getSpv2PersonId());
		}

		HashMap<String, Object> toReplace = new HashMap<String, Object>();
		toReplace.put("toSupervisorName", usersToBeNotified.getEmployeeName());
		toReplace.put("npoSupervisor", usersToBeNotified.getEmployeeNumber());

		toReplace.put("npo", outEmployeeNew.getNpo());
		toReplace.put("employeeName", outEmployeeNew.getFullName());

		toReplace.put("npoSupervisorOne", userOne.getEmployeeNumber());
		toReplace.put("supervisorOne", userOne.getEmployeeName());

		toReplace.put("npoSupervisorTwo", userTwo.getEmployeeNumber());
		toReplace.put("supervisorTwo", userTwo.getEmployeeName());

		toReplace.put("vendorName", "");
		toReplace.put("organizationName", organization.getOrganizationName());
		toReplace.put("effectiveStartDate",
				DateFormatUtils.format(outEmployeeNew.getEffectiveStartDate(), "dd-MMM-yyyy"));
		toReplace.put("effectiveEndDate", DateFormatUtils.format(outEmployeeNew.getEffectiveEndDate(), "dd-MMM-yyyy"));

		UserITMS userSend = userITMSMapper.searchUserByEmpNum(Utils.getSessionUser().getEmployeeNumber().toString());
		toReplace.put("createdBy", userSend.getEmployeeName());

		List<KeyValue> customLinks = new ArrayList<>();
		customLinks.add(new KeyValue(urlTarget, " to see detail."));
		toReplace.put("customLinks", customLinks);
		return toReplace;
	}

	/** CreateGenerateSecurityTicket for get ticket id */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	private ServiceResult<ITSMTicket> createGenerateSecurityTicket(OutEmployee outEmployeeNew, Long executorId,
			Long responsibilityId) {
		ServiceResult<ITSMTicket> result = new ServiceResult<>();
		try {

			List<SecurityTicket> resultSecurityTicket = resourceTransactionMapper.getActiveSecurityTicket(executorId);
			if (resultSecurityTicket.size() == 0) {
				result.addErrorMessage("Can't save security ticket, service catalog category is expired");
				result.setSuccess(false);
				return result;
			}
			SecurityTicket securityTicket = resultSecurityTicket.get(0);

			ServiceResult<IdValue> resultRequestorBranch = itsmTicketService
					.getBranchByPersonId(outEmployeeNew.getSpv1PersonId(), executorId);
			if (!resultRequestorBranch.isSuccess()) {
				result.addErrorMessage(resultRequestorBranch.getFirstErrorMessage());
				result.setSuccess(false);
				return result;

			}

			IdValue branchRequestor = resultRequestorBranch.getResult();
			if (branchRequestor == null) {
				result.addErrorMessage("Can't save Security Ticket, null value when get Requestor Branch By Person Id");
				result.setSuccess(false);
				return result;
			}

			ServiceResult<IdValue> resultRequestorLoc = itsmTicketService
					.getLocationByPersonId(outEmployeeNew.getSpv1PersonId(), executorId);
			if (!resultRequestorLoc.isSuccess()) {
				result.addErrorMessage(resultRequestorLoc.getFirstErrorMessage());
				result.setSuccess(false);
				return result;
			}

			IdValue requestorLocation = resultRequestorLoc.getResult();
			if (requestorLocation == null) {
				result.addErrorMessage(
						"Can't save Security Ticket, null value when get Requestor Location By Person Id");
				result.setSuccess(false);
				return result;
			}

			/** sc level 2 */
			ServiceResult<ServiceCatalog> resultServiceCatalogLvl2 = itsmTicketService
					.getServiceCatalogParentByChildId(securityTicket.getServiceCatalogLevel3Id());
			if (!resultServiceCatalogLvl2.isSuccess()) {
				throw new CustomException(resultServiceCatalogLvl2.getFirstErrorMessage());
			}

			ServiceCatalog serviceCatalogLvl2 = resultServiceCatalogLvl2.getResult();
			if (serviceCatalogLvl2 == null) {
				throw new CustomException(
						"Can't save Security Ticket, null value when get data Service Catalog level 2 "
								+ securityTicket.getServiceCatalogLevel3Id());
			}

			/** sc level 1 */
			ServiceResult<ServiceCatalog> resultServiceCatalogLvl1 = itsmTicketService
					.getServiceCatalogParentByChildId(serviceCatalogLvl2.getServiceCatalogId());
			if (!resultServiceCatalogLvl1.isSuccess()) {
				throw new CustomException(resultServiceCatalogLvl1.getFirstErrorMessage());
			}

			ServiceCatalog serviceCatalogLvl1 = resultServiceCatalogLvl1.getResult();
			if (serviceCatalogLvl1 == null) {
				throw new CustomException(
						"Can't save Security Ticket, null value when get data Service Catalog level 1 "
								+ serviceCatalogLvl2.getServiceCatalogId());
			}

			/** Get Security Ticket Id */

			ServiceResult<ITSMTicketServiceCriticality> resultTiciketCriticality = itsmTicketService
					.getCriticalityByServiceCatalog(securityTicket.getServiceCatalogLevel3Id(), null);
			if (!resultTiciketCriticality.isSuccess()) {
				throw new CustomException(resultTiciketCriticality.getFirstErrorMessage());
			}

			ITSMTicketServiceCriticality criticality = resultTiciketCriticality.getResult();
			if (criticality == null) {
				throw new CustomException(
						"Can't save Security Ticket, null value when get Criticality Id from database");
			}
			String npo = "";
			if (null != outEmployeeNew.getNpo()) {
				if (outEmployeeNew.getNpo().length() > 0) {
					npo = outEmployeeNew.getNpo();
				}
			}

			/** Get catalog category */
			List<Long> ticketTypeId = new ArrayList<>();
			ticketTypeId.add(ITSMTicketTypes.SERVICE.getId().longValue());

			System.out.println(" SC Level 3 " + securityTicket.getServiceCatalogLevel3Id());

			ServiceResult<List<ServiceCatalogCategoryandTicketType>> catalogCategoryResult = itsmTicketService
					.getServiceCatalogCategoriesByServiceCatalog(securityTicket.getServiceCatalogLevel3Id(),
							ticketTypeId);
			if (!catalogCategoryResult.isSuccess() && null == catalogCategoryResult.getResult()) {
				throw new CustomException(catalogCategoryResult.getFirstErrorMessage()
						+ " Null value in result Catalog Catagory :" + securityTicket.getServiceCatalogLevel3Id());
			}

			boolean foundSCServices = false;
			List<ServiceCatalogCategoryandTicketType> lstCatalogCategory = catalogCategoryResult.getResult();
			ServiceCatalogCategoryandTicketType serviceCatalog = new ServiceCatalogCategoryandTicketType();
			for (ServiceCatalogCategoryandTicketType scCategoryandTicketType : lstCatalogCategory) {
				if (scCategoryandTicketType.getTicketType() == ITSMTicketTypes.SERVICE.getId().longValue()) {
					if(securityTicket.getServiceCatalogCategoryId().intValue()==scCategoryandTicketType.getId().intValue()){
						serviceCatalog = scCategoryandTicketType;
						foundSCServices = true;
					}
				}
				if (foundSCServices)
					break;
			}

			if (foundSCServices == false) {
				throw new CustomException(
						"Can't save Generate Scurity Ticket : Not found Service Catalog Type [ Services ]");
			}

			/** set data original ticket */
			ITSMTicket originalTicket = new ITSMTicket();
			originalTicket.setRequestedBy(outEmployeeNew.getSpv1PersonId());
			originalTicket.setRequestorBranchId(branchRequestor.getId());
			originalTicket.setRequestorLocationId(requestorLocation.getId());
			originalTicket.setTicketTitle(securityTicket.getRegisterTicketTitle());

			String ticketDecription = "NPO: " + npo + ", \n" + "Full Name: " + outEmployeeNew.getFullName() + ", \n"
					+ "Effective Start Date: "
					+ DateFormatUtils.format(outEmployeeNew.getEffectiveStartDate(), "dd-MMM-yyyy") + ", \n"
					+ "Effective End Date: "
					+ DateFormatUtils.format(outEmployeeNew.getEffectiveEndDate(), "dd-MMM-yyyy");
			originalTicket
					.setTicketDescription(securityTicket.getRegisterTicketDescription() + ", \n" + ticketDecription);
			originalTicket.setServiceCatalogLevel1Id(serviceCatalogLvl1.getServiceCatalogId());
			originalTicket.setServiceCatalogLevel2Id(serviceCatalogLvl2.getServiceCatalogId());
			originalTicket.setServiceCatalogLevel3Id(securityTicket.getServiceCatalogLevel3Id());
			originalTicket.setServiceCatalogCategoryId(serviceCatalog.getId());
			originalTicket.setTicketTypeId(ITSMTicketTypes.SERVICE.getId().longValue());
			originalTicket.setServiceCriticalityId(criticality.getCriticalityId());
			originalTicket.setRootCauseId(null);
			originalTicket.setRootCauseId(null);
			originalTicket.setTicketSourceId(1L);

			List<DocumentITMS> attachments = null;

			List<ITSMTicketMandatoryField> mandatoryFields = new ArrayList<>();
			ServiceResult<List<ITSMTicketMandatoryField>> mandatoryFieldsResult = itsmTicketService
					.getMandatoryFieldsByServiceCatalog(securityTicket.getServiceCatalogLevel3Id(),
							Utils.getSessionUserId());
			if (mandatoryFieldsResult.isSuccess()) {
				if (null != mandatoryFieldsResult.getResult()) {
					List<ITSMTicketMandatoryField> tmpData = mandatoryFieldsResult.getResult();
					for (int i = 0; i < tmpData.size(); i++) {
						tmpData.get(i).setFieldValue("N/A");
					}
					mandatoryFields.addAll(tmpData);
				}
			}
			List<ITSMRelatedTicket> relatedTickets = null;
			List<ITSMRelatedTicket> deletedRelatedTickets = null;
			ItsmTicketActions action = ItsmTicketActions.ASSIGN_BY_SERVICE_CATALOG;
			ItsmRoles targetRole = null;
			UserITMS targetAssignee = null;
			String remarks = "Generated Security Ticket";

			/**
			 * mendapatkan mandatory field dari SC level 3 masuk menu ITSM Setup
			 * Setup Aditional Ticket Information Cek service catalog tittle Cek
			 * Mandatory Filed System.out.println("Supervisor 1   :"
			 * +outEmployeeNew.getSpv1PersonId()); System.out.println("Branch "
			 * +branchRequestor.getId()); System.out.println(
			 * "Requestor Lock    "+requestorLocation.getId());
			 * System.out.println("Ticket Tittle    "
			 * +securityTicket.getRegisterTicketTitle()); System.out.println(
			 * "Tittle Desc      "+ticketDecription); System.out.println(
			 * "Register Ticket Des   "
			 * +securityTicket.getRegisterTicketDescription());
			 * System.out.println("SC 1   "
			 * +serviceCatalogLvl1.getServiceCatalogId()); System.out.println(
			 * "SC 2   "+serviceCatalogLvl2.getServiceCatalogId());
			 * System.out.println("SC 3   "
			 * +securityTicket.getServiceCatalogLevel3Id());
			 */

			ServiceResult<ITSMTicket> saveResult = itsmTicketService.saveCreateTicket(originalTicket, attachments,
					mandatoryFields, relatedTickets, deletedRelatedTickets, action, targetRole, targetAssignee, remarks,
					responsibilityId, executorId);

			if (!saveResult.isSuccess()) {
				result.setSuccess(false);
				result.addErrorMessage(saveResult.getFirstErrorMessage());
				return result;
			}
			ITSMTicket itsmTicket = saveResult.getResult();
			if (itsmTicket == null) {
				result.setSuccess(false);
				result.addErrorMessage(
						"Can't save Generate Ticket, Null value in result save generate security ticket");
				return result;
			}
			result.setSuccess(true);
			result.setResult(itsmTicket);
		} catch (CustomException e) {
			e.printStackTrace();
			result.setSuccess(false);
			result.addErrorMessage(e.getMessage());
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			debugLogService.writeErrorLog(e, Utils.getSessionUserId(), getClass());
		} catch (Exception e) {
			e.printStackTrace();
			result.setSuccess(false);
			result.addErrorMessage("unhadler.error");
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			debugLogService.writeErrorLog(e, Utils.getSessionUserId(), getClass());
		}
		return result;
	}



	


	end register outsource 



	@Transactional
	@Override
	public ServiceResult<OutEmployee> saveReviewOutsource(OutEmployee outEmployee,
			EmployeePersonalInformation empPersonalInfo, 
			List<EmployeeLob> relatedLob,
			List<EmployeeFunction> employeeFunctions, 
			List<EmployeeCertification> employeeCertifications,
			List<DocumentITMS> documentCertificates, 
			List<EmployeeCompetency> employeeCompetencies,
			List<Long> lstRemoveRelatedLOB, 
			List<Long> lstRemoveFunction, 
			List<Long> lstRemoveCertificates,
			List<Long> lstRemoveAttachments, 
			List<Long> lstRemoveCompetency,
			 Long executorId, 
			 Long responsibilityId) {

		ServiceResult<OutEmployee> result = new ServiceResult<>();
		try {
			OutEmployee outEmployeeNew = new OutEmployee(outEmployee);

			List<OutEmployee> regCheckDuplicated = new ArrayList<>();
			if (null != outEmployeeNew.getNpo() && !outEmployeeNew.getNpo().isEmpty() && outEmployeeNew.getNpo().trim().length() != 0) {
				regCheckDuplicated = resourceTransactionMapper.getOutsourceEmployeeByNPO(outEmployeeNew.getNpo().trim());
			}

			boolean found = false;
			for (OutEmployee employee : regCheckDuplicated) {
				if (employee.getOutEmployeeId().longValue() != outEmployeeNew.getOutEmployeeId().longValue()) {
					found = true;
					break;
				}
			}

			if (found == true) {
				result.addErrorMessage("NPO already exist");
				result.setSuccess(false);
				return result;
			}

			// NPO must not contain numbers only
			// will checked if npo contain values
			Boolean isNotContainNumericOnly = Boolean.FALSE;
			if (null != outEmployeeNew.getNpo() && !outEmployeeNew.getNpo().isEmpty()
					&& outEmployeeNew.getNpo().trim().length() != 0) {
				boolean isNumeric = CheckNpoIsNumerics(outEmployeeNew.getNpo());
				if (isNumeric == true) {
					isNotContainNumericOnly = Boolean.TRUE;
				} else {
					isNotContainNumericOnly = Boolean.FALSE;
				}
			}

			if (isNotContainNumericOnly == Boolean.TRUE) {
				result.addErrorMessage("NPO must not contain numbers only");
				result.setSuccess(false);
				return result;
			}
			// check employee last update
			OutEmployee lastUpdateOutEmployee = resourceTransactionMapper.getActiveOutEmployeeById(outEmployeeNew.getOutEmployeeId());
			if (lastUpdateOutEmployee != null && outEmployeeNew.getLastUpdateDate() != null) {
				if (CustomDateUtils.compareBetweenDate(lastUpdateOutEmployee.getLastUpdateDate(),outEmployeeNew.getLastUpdateDate(), false) > 0) 
				{
					throw new CustomException("Outsource Employee has been changed by someone else. Please close and reopen this page");
				}
			}

			int rowAffected = 0;
			/** UPDATE OUTSOURCE EMPLOYEE **/
			rowAffected = resourceTransactionMapper.saveReviewOutsource(outEmployeeNew, executorId);

			/** Jika uploaded Photo */
			String fileName = null;
			if (outEmployeeNew.getEmployeePhoto() != null) {
				/** set path photo */
				String path = PathFileEnum.REGISTER_PHOTO_FILE.getName();
				String extensionFile = outEmployeeNew.getExtensionPhoto();

				if (extensionFile != null) {
					/**
					 * jika tidak merubah foto, maka extension akan berisi null
					 * sehingga tidak perlu di update
					 */
					fileName = path + String.valueOf(outEmployeeNew.getOutEmployeeId()).replace("-", "Out")
							+ extensionFile;

					outEmployeeNew.setEmployeePhoto(fileName);
					rowAffected = resourceTransactionMapper.updateRegisterOutsourceImageName(outEmployeeNew);
				}
			}

			EmployeePersonalInformation outEmpInformation = resourceTransactionMapper
					.getOutEmployeeInformationById(outEmployeeNew.getOutEmployeeId());
			if (outEmpInformation == null) {
				/** INSERT PERSONAL INFORMATION **/
				EmployeePersonalInformation employeePersonalInformation = new EmployeePersonalInformation(
						empPersonalInfo);
				employeePersonalInformation.setOutEmployeeId(outEmployeeNew.getOutEmployeeId());
				rowAffected = resourceTransactionMapper.insertEmployeePersonalInformation(employeePersonalInformation,
						executorId);
			} else {
				/** Update personal informations */
				EmployeePersonalInformation employeePersonalInformation = new EmployeePersonalInformation(
						empPersonalInfo);
				employeePersonalInformation.setOutEmployeeId(outEmployeeNew.getOutEmployeeId());
				rowAffected = resourceTransactionMapper.updateEmployeeInformation(employeePersonalInformation,
						executorId);
			}

			/** INSERT OR UPDATE RELATED LOB */
			if (relatedLob != null) {
				List<EmployeeLob> lstEmployeeLobs = new ArrayList<>();
				lstEmployeeLobs.addAll(relatedLob);
				for (EmployeeLob employeeLob : lstEmployeeLobs) {
					if (employeeLob.getHasChange() == true) {
						employeeLob.setEmployeeId(outEmployeeNew.getOutEmployeeId());
						if (employeeLob.getOutEmployeeLobsId() == -1L) {
							rowAffected = resourceTransactionMapper.insertRelatedLOB(employeeLob, executorId);
						} else {
							Long lobId = employeeLob.getLobId();
							Long outEmployeeLobsId = employeeLob.getOutEmployeeLobsId();
							rowAffected = resourceTransactionMapper.updateRelatedLOB(lobId, executorId,
									outEmployeeLobsId);

						}
					}
				}
			}

			/** INSERT or UPDATE EMPLOYEE FUNCTION */
			if (employeeFunctions != null) {
				List<EmployeeFunction> lstEmployeeFunction = new ArrayList<>();
				lstEmployeeFunction.addAll(employeeFunctions);
				for (EmployeeFunction employeeFunction : lstEmployeeFunction) {
					if (employeeFunction.getHasChange() == true) {
						employeeFunction.setEmployeeId(outEmployeeNew.getOutEmployeeId());
						if (employeeFunction.getOutEmployeeFunctionId() == -1L) {
							rowAffected = resourceTransactionMapper.insertEmployeeFunction(employeeFunction,executorId);
						} else {
							Long functionId = employeeFunction.getFunctionId();
							Long outEmployeeFunctionId = employeeFunction.getOutEmployeeFunctionId();
							rowAffected = resourceTransactionMapper.updateEmployeeFunction(functionId, executorId,outEmployeeFunctionId);
						}
					}
				}
			}

			/** INSERT OR UPDATE CERTIFICATES */
			if (employeeCertifications != null) {
				List<EmployeeCertification> lstEmployeeCertification = new ArrayList<>();
				lstEmployeeCertification.addAll(employeeCertifications);
				for (EmployeeCertification employeeCertification : lstEmployeeCertification) {
					if (employeeCertification.getIsHasChange() == true) {
						employeeCertification.setEmployeeId(outEmployeeNew.getOutEmployeeId());
						if (employeeCertification.getCertificationId() == -1L) {
							rowAffected = resourceTransactionMapper.insertEmpOutCertifications(employeeCertification,executorId);
						} else {
							rowAffected = resourceTransactionMapper.updateEmployeeCertification(employeeCertification,executorId);
						}
					}
				}
			}

			/**
			 * INSERT DOCUMENT CERTIFICATES {tidak ada update, yang telah ada di ignore}
			 */
			if (documentCertificates != null) {
				List<DocumentITMS> documentCertificate = new ArrayList<>();
				List<EmployeeCertificates> lstEmployeeCertificates = new ArrayList<>();
				for (DocumentITMS documentITMS : documentCertificates) {
					if (documentITMS.getDocumentId() == null) {
						documentITMS.setDocumentName(documentITMS.getDocumentName());
						documentITMS.setDocumentPath(documentITMS.getDocumentPath());
						rowAffected = resourceTransactionMapper.insertDumpDocumentFile(documentITMS, executorId);
						EmployeeCertificates empCertificate = new EmployeeCertificates();
						empCertificate.setOutEmployeeId(outEmployeeNew.getOutEmployeeId());
						empCertificate.setDocumentId(documentITMS.getDocumentId());
						lstEmployeeCertificates.add(empCertificate);
					}
				}
				for (EmployeeCertificates employeeCertificates : lstEmployeeCertificates) {
					if (employeeCertificates.getCertificateId() == null) {
						rowAffected = resourceTransactionMapper.insertDocumentCertificateFiles(employeeCertificates,executorId);
					}
				}
			}

			/** INSERT COMPETENCY */
			if (employeeCompetencies != null) {
				for (EmployeeCompetency employeeCompetency : employeeCompetencies) {
					if (employeeCompetency.getHasChange() == true) {
						employeeCompetency.setEmployeeId(outEmployeeNew.getOutEmployeeId());
						if (employeeCompetency.getEmployeeCompetenciesId() == -1L) {
							rowAffected = resourceTransactionMapper.insertEmployeeCompetency(employeeCompetency,executorId);
						} else {
							rowAffected = resourceTransactionMapper.updateEmployeeCompetency(employeeCompetency,executorId);
						}
					}
				}
			}
			/** END INSERT */

			if (lstRemoveRelatedLOB.size() != 0) {
				for (int i = 0; i < lstRemoveRelatedLOB.size(); i++) {
					Long outEmployeeLobsId = lstRemoveRelatedLOB.get(i);
					if (outEmployeeLobsId != -1L) {
						rowAffected = resourceTransactionMapper.deleteRelatedLOB(executorId, outEmployeeLobsId);
					}
				}
			}
			if (lstRemoveFunction.size() != 0) {
				for (int i = 0; i < lstRemoveFunction.size(); i++) {
					Long outEmployeeFunctionId = lstRemoveFunction.get(i);
					if (outEmployeeFunctionId != -1L) {
						rowAffected = resourceTransactionMapper.deleteEmployeeFunction(executorId,outEmployeeFunctionId);
					}
				}
			}
			if (lstRemoveCertificates.size() != 0) {
				for (int i = 0; i < lstRemoveCertificates.size(); i++) {
					Long certificationId = lstRemoveCertificates.get(i);
					if (certificationId != -1L) {
						rowAffected = resourceTransactionMapper.deleteEmployeeCertification(executorId,certificationId);
					}
				}
			}
			if (lstRemoveAttachments.size() != 0) {
				for (int i = 0; i < lstRemoveAttachments.size(); i++) {
					Long documentId = lstRemoveAttachments.get(i);
					if (documentId != null) {
						rowAffected = resourceTransactionMapper.deleteDocumentCertificateFiles(executorId, documentId);
						rowAffected = resourceTransactionMapper.deleteDocumentFiles(executorId, documentId);
					}
				}
			}

			if (lstRemoveCompetency.size() != 0) {
				for (int i = 0; i < lstRemoveCompetency.size(); i++) {
					Long employeeCompetenciesId = lstRemoveCompetency.get(i);
					rowAffected = resourceTransactionMapper.deleteEmployeeCompetencyByEmployeeCompetencyId(employeeCompetenciesId, executorId);
				}
			}

			result.setSuccess(true);
			result.setResult(outEmployeeNew);
		} catch (CustomException e) {
			e.printStackTrace();
			result.setSuccess(false);
			result.addErrorMessage(e.getMessage());
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			debugLogService.writeErrorLog(e, Utils.getSessionUserId(), getClass());
		} catch (Exception e) {
			e.printStackTrace();
			result.setSuccess(false);
			result.addErrorMessage("unhadler.error");
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			debugLogService.writeErrorLog(e, Utils.getSessionUserId(), getClass());
		}
		return result;
	}

	private boolean CheckNpoIsNumerics(String npo) {
		for (int ctr = 0; ctr < npo.length(); ctr++) {
			if ("1234567890".contains(Character.valueOf(npo.charAt(ctr)).toString())) {
				continue;
			} else {
				return false;
			}
		}
		return true;
	}
}


@Override
	public ServiceResult<Boolean> sendNeedApprovalNotifyAndMailToSVPByEmpAndStatus(
			List<MonthlyWorksheetNotification> lstMonthlyWorksheet,
			OutEmployee employee,
			String userSender,
			Long currentAssignedSupervisorId, 
			String serverAddress,
			ITMSNotificationTemplate template, 
			String currentStatus, 
			Long executorId, 
			Long responsibilityId) {

		ServiceResult<Boolean> result = new ServiceResult<>();
		try {
			/**
			 * user to be sender dalam hal ini adalah supervisor
			 * menggunakan supervisorId untuk mendapatkan employe Numbber dari supervisor tersebut
			 */
			UserITMS usersToBeNotified = userITMSMapper.getUserByPersonId(currentAssignedSupervisorId);
			
			/** digunakan untuk sendTo(....) ke supervisor */
			UserITMS userToNotify = userITMSMapper.searchUserByEmpNum(usersToBeNotified.getEmployeeNumber().toString());
			
			for (MonthlyWorksheetNotification monthlyWorksheetNotification : lstMonthlyWorksheet) {
				
				Long monthlyWorksheetId=monthlyWorksheetNotification.getMonthlyWorksheetId();
				/** URL Target */
				String urlTarget = ITMSPages.CREATE_REVIEW_MONTHLY_WORKSHEET.getUrl() + "?id=" + monthlyWorksheetId+"&type=READONLY&caller=NOTIFICATION";

				Long personId = employee.getOutEmployeeId();

				/** replace template untuk send dashboard and email */
				HashMap<String, Object> mapTemplate = replaceTemplateMonthlyWorksheetByParameter(monthlyWorksheetNotification,personId,
						urlTarget,
						currentStatus, 
						usersToBeNotified, 
						executorId,
						currentAssignedSupervisorId);

				String subject = Utils.replaceTemplate(template.getTemplateSubject(), mapTemplate);
				String message = Utils.replaceTemplate(template.getTemplateContent(), mapTemplate);

				/***Send dashbord to be notify (Supervisor ) 
				 *  Cara DIBAWAHini tidak dapat digunakan lagi jika menggunakan event queue
				 *  notificationManagerService.sendNotification(subject, 
				 *          message, 
				 *          urlTarget, 
				 *          MessageType.FYI_MESSAGE,
				 *          usersToBeNotified, 
				 *          executorId);
				 *  userSender berisi  employee number orang yang mengirimkan */
				UserITMS fromUser = userITMSMapper.searchUserByEmpNum(userSender);
				
				/*** send dashboard to be notify (Supervisor ) ***/
				NotificationMessage notification = prepareNotificationMessage(subject, message, urlTarget, MessageType.FYI_MESSAGE);
				notification.setFromId((UUID)fromUser.getPersonUUID());
				notification.setToId((UUID)userToNotify.getPersonUUID());
				notificationMessageDAO.insertNewMessage(notification);
				
				
				/*** send email to be notify (Supervisor ) ***/
				EmailContentTemplate emailContentTemplateOne = new EmailContentTemplate();
				emailContentTemplateOne.setTemplate(template);
				emailContentTemplateOne.setReplacement(mapTemplate);
				emailContentTemplateOne.setTo(usersToBeNotified);
				List<EmailContentTemplate> emailsToSend = new ArrayList<>();
				emailsToSend.add(emailContentTemplateOne);
				emailSenderService.sendEmails(emailsToSend, EmailSendType.DIRECT, serverAddress, executorId);
			}
			result.setResult(true);
			result.setSuccess(true);
		} catch (CustomException e) {
			e.printStackTrace();
			result.setSuccess(false);
			result.addErrorMessage(e.getMessage());
			debugLogService.writeErrorLog(e, Utils.getSessionUserId(), getClass());
		} catch (Exception e) {
			e.printStackTrace();
			result.setSuccess(false);
			result.addErrorMessage("unhadler.error");
			debugLogService.writeErrorLog(e, Utils.getSessionUserId(), getClass());
		}
		return result;
	}

	public HashMap<String, Object> replaceTemplateMonthlyWorksheetByParameter(MonthlyWorksheetNotification monthlyWorksheet,Long personId, String urlTarget,
			String currentStatus, 
			UserITMS usersToBeNotified, 
			Long executorId,
			Long currentAssignedSupervisorId) throws CustomException {
		
		String year = String.valueOf(monthlyWorksheet.getYear() + "");
		HashMap<String, Object> toReplace = new HashMap<String, Object>();
		// notify to
		toReplace.put("employeeNameReceiper", usersToBeNotified.getEmployeeName());
		toReplace.put("npkReceiper", usersToBeNotified.getEmployeeNumber());

		// employee
		toReplace.put("npo", monthlyWorksheet.getNpo());
		toReplace.put("employeeName", monthlyWorksheet.getFullName());

		toReplace.put("month", monthlyWorksheet.getFormatMonth());
		toReplace.put("yyyy", year);

		toReplace.put("currentStatus", currentStatus);
		toReplace.put("vendorName", monthlyWorksheet.getVendorName());
		toReplace.put("organizationName", monthlyWorksheet.getOrganizationName());

		UserITMS userSend = userITMSService.getUserById(executorId);
		toReplace.put("createdBy", userSend.getEmployeeName());

		List<KeyValue> customLinks = new ArrayList<>();
		customLinks.add(new KeyValue(urlTarget, " to see detail."));
		toReplace.put("customLinks", customLinks);
		return toReplace;
	}


	@Override
	public ServiceResult<Boolean> sendMailRegistrasiOutsourceToSVP(OutEmployee outEmployeeNew, String serverAddress,
			Long responsibilityId, Long executorId) {
		ServiceResult<Boolean> result = new ServiceResult<>();

		try {
			ServiceResult<ITMSNotificationTemplate> svcTemplateResult = notificationManagerService
					.getNotificationTemplate("REGISTER_OUT", "REGISTER_OUT", executorId);
			if (!svcTemplateResult.isSuccess()) {
				MessagePopupUtils.error(svcTemplateResult.getFirstErrorMessage(), null);
				result.setSuccess(false);
				result.setResult(false);
				return result;
			} else {

				/** get template from db */
				ITMSNotificationTemplate template = svcTemplateResult.getResult();

				if (template == null) {
					MessagePopupUtils.error("Template Email Register Outsource is not Found", null);
					result.setSuccess(false);
					result.setResult(false);
					return result;
				}

				UserITMS usersToBeNotified = userITMSMapper.getUserByPersonId(outEmployeeNew.getSpv1PersonId());

				String urlTarget = ITMSPages.REVIEW_REGISTER_OUTSOURCE_EMPLOYEE.getUrl() + "?personId="
						+ outEmployeeNew.getOutEmployeeId() + "&type=EDIT&caller=NOTIFICATION";

				/** get template for replace */

				/** ServrevAddress */

				/****/
				HashMap<String, Object> mapTemplate = templEmailRegisterOutsource(outEmployeeNew, urlTarget,
						usersToBeNotified, executorId);

				System.out.println("mapTemplate   " + mapTemplate);
				EmailContentTemplate emailContentTemplateOne = new EmailContentTemplate();
				emailContentTemplateOne.setTemplate(template);
				emailContentTemplateOne.setReplacement(mapTemplate);
				emailContentTemplateOne.setTo(usersToBeNotified);
				List<EmailContentTemplate> emailsToSend = new ArrayList<>();
				emailsToSend.add(emailContentTemplateOne);
				emailSenderService.sendEmails(emailsToSend, EmailSendType.DIRECT, serverAddress, executorId);

				if (outEmployeeNew.getSpv2PersonId() != null) {
					UserITMS usersToBeNotified2 = userITMSMapper.getUserByPersonId(outEmployeeNew.getSpv2PersonId());
					HashMap<String, Object> mapTemplate2 = templEmailRegisterOutsource(outEmployeeNew, urlTarget,
							usersToBeNotified2, executorId);

					UserITMS usersToBeNotifiedSp2 = userITMSMapper.getUserByPersonId(outEmployeeNew.getSpv2PersonId());

					EmailContentTemplate emailContentTemplateTwo = new EmailContentTemplate();
					emailContentTemplateTwo.setTemplate(template);
					emailContentTemplateTwo.setReplacement(mapTemplate2);
					emailContentTemplateTwo.setTo(usersToBeNotifiedSp2);
					List<EmailContentTemplate> emailsToSendTwo = new ArrayList<>();
					emailsToSendTwo.add(emailContentTemplateTwo);
					emailSenderService.sendEmails(emailsToSendTwo, EmailSendType.DIRECT, serverAddress, executorId);
				}
			}

			result.setSuccess(true);
			result.setResult(true);
		} catch (Exception e) {
			e.printStackTrace();
			result.setSuccess(false);
			debugLogService.writeErrorLog(e, executorId, getClass());
		}
		return result;
	}
