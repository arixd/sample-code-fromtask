package id.co.fifgroup.fifgroup_rm.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.print.attribute.standard.MediaSize.Other;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Fileupload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabs;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import id.co.fifgroup.fifgroup_itms.global.constant.FormMode;
import id.co.fifgroup.fifgroup_itms.global.constant.ITMSPages;
import id.co.fifgroup.fifgroup_itms.global.constant.ItsmRoles;
import id.co.fifgroup.fifgroup_itms.global.constant.ItsmTicketActions;
import id.co.fifgroup.fifgroup_itms.global.controller.BaseFormComposer;
import id.co.fifgroup.fifgroup_itms.global.model.DocumentITMS;
import id.co.fifgroup.fifgroup_itms.global.model.EmployeeLocation;
import id.co.fifgroup.fifgroup_itms.global.model.Function;
import id.co.fifgroup.fifgroup_itms.global.model.ITSMTicket;
import id.co.fifgroup.fifgroup_itms.global.model.IdValue;
import id.co.fifgroup.fifgroup_itms.global.model.LineOfBusiness;
import id.co.fifgroup.fifgroup_itms.global.model.OrganizationITMS;
import id.co.fifgroup.fifgroup_itms.global.model.UserITMS;
import id.co.fifgroup.fifgroup_itms.util.FileUtils;
import id.co.fifgroup.fifgroup_itms.util.MessageBoxIcon;
import id.co.fifgroup.fifgroup_itms.util.MessagePopupUtils;
import id.co.fifgroup.fifgroup_itms.util.ServiceResult;
import id.co.fifgroup.fifgroup_itms.util.Utils;
import id.co.fifgroup.fifgroup_itms.util.Utils.SortDirection;
import id.co.fifgroup.fifgroup_itsm.model.ITSMRelatedTicket;
import id.co.fifgroup.fifgroup_itsm.service.ITSMTicketService;
import id.co.fifgroup.fifgroup_rm.constanta.BloodTypeEnum;
import id.co.fifgroup.fifgroup_rm.constanta.GenderEnum;
import id.co.fifgroup.fifgroup_rm.constanta.PathFileEnum;
import id.co.fifgroup.fifgroup_rm.constanta.SubFormRegsiterOutsourceEnum;
import id.co.fifgroup.fifgroup_rm.model.EmployeeCertificates;
import id.co.fifgroup.fifgroup_rm.model.EmployeeCertification;
import id.co.fifgroup.fifgroup_rm.model.EmployeeFunction;
import id.co.fifgroup.fifgroup_rm.model.EmployeeLob;
import id.co.fifgroup.fifgroup_rm.model.EmployeePersonalInformation;
import id.co.fifgroup.fifgroup_rm.model.OutEmployee;
import id.co.fifgroup.fifgroup_rm.service.ResourceService;
import id.co.fifgroup.fifgroup_rm.viewmodel.RegisterOutsourceEmployeeViewModel;

@SuppressWarnings({ "rawtypes", "unchecked" })
@VariableResolver(DelegatingVariableResolver.class)
public class RegisterOutsourceEmployeeComposer extends BaseFormComposer {

	private static final long serialVersionUID = 6144126273681327132L;

	@Wire
	private Button btnBrowse;

	@Wire
	private Textbox txtNpo;

	@Wire
	private Label txtVendorRelatedContract;

	@Wire
	private Textbox txtFullName;

	@Wire
	private Textbox txtOrganizationId;

	@Wire
	private Textbox txtSupervisiorOneId;

	@Wire
	private Textbox txtEmployeeLocationId;

	@Wire
	private Textbox txtSupervisorTwoId;

	@Wire
	private Textbox txtLokerNumber;

	@Wire
	private Datebox txtEffectiveStartDate;

	@Wire
	private Datebox effectiveEndDate;
	
	@Wire Datebox txtTerminatedDate;

	@Wire
	private Bandbox bdbOrganization;

	@Wire
	private Bandbox bdbSupervisorOne;

	@Wire
	private Bandbox bdbSupervisorTwo;

	@Wire
	private Bandbox bdbEmployeeLocation;

	@Wire
	private Checkbox chkTerminate;

	@Wire
	private Listbox lstboxSubFormResource;

	@Wire 
	private Label txtFlagCreateSecurityTicket;
	
	/** general informations */
	@Wire
	private Grid showHidePersonalInformation;

	@Wire
	private Textbox txtEmail;

	@Wire
	private Textbox txtEmergencyContactName;

	@Wire
	private Textbox txtAddress;

	@Wire
	private Textbox txtEmergencyContactNumber;

	@Wire
	private Textbox txtEmergencyContRelation;

	@Wire
	private Datebox txtDateOfBirth;

	@Wire
	private Listbox cmbGender;

	@Wire
	private Listbox cmbBloodType;

	@Wire
	private Textbox txtPhoneNumber;

	@Wire
	private Div errOrganization;

	@Wire
	private Div errSupervisorOne;

	@Wire
	private Div errEffectiveStartDate;

	@Wire
	private Div errEffectiveEndDate;

	@Wire
	private Div errFullName;

	@Wire
	private Div errListComponentRegisterOutsource;

	/** listbox related outsources */

	@Wire
	private Listbox lstboxRelatedLOB;

	@Wire
	private Button btnAddRowLob;

	@Wire
	private Button btnDeleteRowLob;

	/** listbox function */
	@Wire
	private Listbox lstboxFunction;

	@Wire
	private Button btnAddRowFunction;

	@Wire
	private Button btnDeleteRowFunction;

	/** listbox certifications */
	@Wire
	private Listbox lstboxCertifications;
	@Wire
	private Button btnAddRowCertifications;

	@Wire
	private Button btnDeleteRowCertifications;

	@Wire
	private Button btnSave;

	/** Upload File Document */
	@Wire
	private Groupbox grbRegisterOutsourceFiles;
	@Wire
	private Fileupload btnFileuploadAttachmentCertificates;
	@Wire
	private Listbox lsbAttachmentCertificates;

	@Wire
	private Button btnDeleteAttachmentCertificates;
	@Wire
	private Label txtEmployeePhoto;
	@Wire
	private Label txtFileNameWithoutExt;
	@Wire
	private Label txtExt;
	@Wire 
	private Vlayout pics;
	@Wire 
	private Media uploadedFile;
	@Wire
	private Button btnUpload;
	
	/** group visible */
	@Wire 
	private Div visibleRetaledLob;
	@Wire 
	private Div visibleFunction;
	@Wire 
	private Div visibleCertifications;
	//@Wire 
	//private Div visibleOutsourceCertificateFile;
	
	@WireVariable(rewireOnActivate = true)
	private transient ITSMTicketService itsmTicketService;
	
	@WireVariable(rewireOnActivate=true)
	private transient ResourceService resourceService;
	
	@WireVariable
	private HashMap<String, Object> arg;
	
	@Override
	protected void setDefaultField() {

		RegisterOutsourceEmployeeViewModel viewModel = new RegisterOutsourceEmployeeViewModel();
		mapComponentsToViewModel(viewModel);
		setDefaultDate(viewModel);
		/** default combobox */
		setDefaultAllComponentSubFormAndAllCmbbox(viewModel);
		/** set default all listbox sub form */
		setVisibleOnChangeSubForm(viewModel);
		bindViewModelToComponents(viewModel);
		setItemRenderRelatedLOB();
		setItemRenderFunctionEmployee();
		setItemRenderCertifications();
		setItemRenderFileCertificates();
		setItemRenderAttachmentFiles();
	}

	private void setItemRenderAttachmentFiles() {

		lsbAttachmentCertificates.setItemRenderer(new ListitemRenderer<DocumentITMS>() {
			@Override
			public void render(Listitem item, DocumentITMS data, int index)
					throws Exception {
				Listcell checkListcell = new Listcell();
				item.appendChild(checkListcell);
				
				Listcell nameListcell = new Listcell();
				Label nameLabel = new Label();
				if(null != data.getDocumentName()){
					nameLabel.setValue(data.getDocumentName());
				}
				nameListcell.appendChild(nameLabel);
				item.appendChild(nameListcell);
			}
		});
	}

	private void setDefaultDate(RegisterOutsourceEmployeeViewModel viewModel) {
		Date date = new Date();
		viewModel.setTxtEffectiveStartDate(LocalDate.now().toDate());
		viewModel.setEffectiveEndDate(LocalDate.parse("4712-12-31").toDate());
	}

	private void setDefaultAllComponentSubFormAndAllCmbbox(RegisterOutsourceEmployeeViewModel viewModel) {
		
		// SET GROUPBOX PERSONAL INFORMATION
		viewModel.setChkTerminate(0L);
		viewModel.setTxtTerminatedDate(null);
		
		List<IdValue> cmbSubForm = SubFormRegsiterOutsourceEnum.getAllSubFormResource();
		viewModel.setLstboxSubFormResource(new ListModelList<>(cmbSubForm));
		viewModel.setLstboxSubFormResourceId(1L);
		// Gender
		List<IdValue> cmbGenderType = GenderEnum.getAllGenderType();
		cmbGenderType.add(0, new IdValue(-1L, "- Please Select -"));
		viewModel.setCmbGender(new ListModelList<>(cmbGenderType));
		viewModel.setCmbGenderId(-1L);
		// Blood Type
		List<IdValue> cmbBloodType = BloodTypeEnum.getAllBloodType();
		cmbBloodType.add(0, new IdValue(-1L, "- Please Select -"));
		viewModel.setCmbBloodType(new ListModelList<>(cmbBloodType));
		viewModel.setCmbBloodTypeId(-1L);

		// SET GROUPBOX SUB FORM RELATED LOB
		ListModelList<EmployeeLob> modelListLOB = new ListModelList<>();
		modelListLOB.setMultiple(true);
		viewModel.setLstboxRelatedLOB(modelListLOB);

		// SET GROUPBOX SUB FORM FUNCTION
		ListModelList<EmployeeFunction> modelListFunction = new ListModelList<EmployeeFunction>();
		modelListFunction.setMultiple(true);
		viewModel.setLstboxFunction(modelListFunction);

		// SET GROUPBOX SUB FORM CERTIFICATED
		ListModelList<EmployeeCertification> modelListCertifications = new ListModelList<>();
		modelListCertifications.setMultiple(true);
		viewModel.setLstboxCertifications(modelListCertifications);
		
		//SET GROUP BOX ATTACHMENT
		ListModelList<DocumentITMS> modelListDocument=new ListModelList<>();
		modelListCertifications.setMultiple(true);
		viewModel.setDocumentModel(modelListDocument);
		
		setVisibleSubForm();
	}


	private void setVisibleOnChangeSubForm(RegisterOutsourceEmployeeViewModel viewModel) {
		errListComponentRegisterOutsource.setVisible(false);
	}

	private void setItemRenderRelatedLOB() {

		lstboxRelatedLOB.setItemRenderer(new ListitemRenderer<EmployeeLob>() {

			@Override
			public void render(Listitem list, EmployeeLob data, int index) throws Exception {
				Listcell cllCheckbox = new Listcell();
				list.appendChild(cllCheckbox);

				Listcell cllno = new Listcell();
				Label lblNo = new Label();
				lblNo.setValue(index + 1 + "");
				cllno.appendChild(lblNo);
				list.appendChild(cllno);

				// cell bandbox competency
				Listcell cllLobNumber = new Listcell(data.getLobNumber());
				list.appendChild(cllLobNumber);

				// cell bandbox LOB
				Listcell cllLobName = new Listcell();
				final Bandbox bdbLobName = new Bandbox();
				bdbLobName.setWidth("100%");
				bdbLobName.setReadonly(true);
				bdbLobName.addForward("onOpen", lstboxRelatedLOB, "onOpenbdbLob", data);
				bdbLobName.setValue(data.getLobName());
				cllLobName.appendChild(bdbLobName);
				list.appendChild(cllLobName);

			}
		});
	}

	private void setItemRenderFunctionEmployee() {
		lstboxFunction.setItemRenderer(new ListitemRenderer<EmployeeFunction>() {

			@Override
			public void render(Listitem list, EmployeeFunction data, int index) throws Exception {
				Listcell cllCheckbox = new Listcell();
				list.appendChild(cllCheckbox);

				Listcell cllno = new Listcell();
				Label lblNo = new Label();
				lblNo.setValue(index + 1 + "");
				cllno.appendChild(lblNo);
				list.appendChild(cllno);

				// cell bandbox competency
				Listcell cllFunctionNumber = new Listcell(data.getFunctionNumber());
				list.appendChild(cllFunctionNumber);

				// cell bandbox standard competency
				Listcell cllFunctionName = new Listcell();
				final Bandbox bdbFunctionName = new Bandbox();
				bdbFunctionName.setWidth("100%");
				bdbFunctionName.setReadonly(true);
				bdbFunctionName.addForward("onOpen", lstboxFunction, "onOpenbdbFunction", data);
				bdbFunctionName.setValue(data.getFunctionName());
				cllFunctionName.appendChild(bdbFunctionName);
				list.appendChild(cllFunctionName);
			}
		});
	}

	private void setItemRenderCertifications() {
		lstboxCertifications.setItemRenderer(new ListitemRenderer<EmployeeCertification>() {

			@Override
			public void render(Listitem list, final EmployeeCertification data, int index) throws Exception {
				Listcell cllCheckbox = new Listcell();
				list.appendChild(cllCheckbox);

				Listcell cllno = new Listcell();
				Label lblNo = new Label();
				lblNo.setValue(index + 1 + "");
				cllno.appendChild(lblNo);
				list.appendChild(cllno);

				// cell bandbox competency
				Listcell cllCertificationName = new Listcell();
				Textbox txtTrainingName = new Textbox();
				txtTrainingName.setMaxlength(150);
				txtTrainingName.setValue(data.getTrainingName());
				txtTrainingName.setWidth("100%");
				txtTrainingName.addEventListener("onChange", new EventListener<Event>() {
					@Override
					public void onEvent(Event arg0) throws Exception {
						Textbox trainingName = (Textbox) arg0.getTarget();
						data.setTrainingName(trainingName.getValue().isEmpty() || trainingName.getValue().trim().length()==0?null:trainingName.getValue().trim());
						data.setIsHasChange(true);
						setHasChanged(true);
					}
				});
				cllCertificationName.appendChild(txtTrainingName);
				list.appendChild(cllCertificationName);

				// checkbox
				Listcell cllCertified = new Listcell();
				final Checkbox checkbox = new Checkbox();
				if (data.getIsCertified() == 1) {
					checkbox.setChecked(true);
				}
				checkbox.addEventListener("onCheck", new EventListener<Event>() {
					@Override
					public void onEvent(Event arg0) throws Exception {
						if (checkbox.isChecked()) {
							data.setIsCertified(1);
						} else {
							data.setIsCertified(0);
						}
						setHasChanged(true);
					}
				});
				checkbox.setStyle("text-align: center;");
				cllCertified.appendChild(checkbox);
				cllCertified.setStyle("text-align: center;");
				list.appendChild(cllCertified);

				// start date
				Listcell lclDateYear = new Listcell();
				final Datebox dateYear = new Datebox();
				dateYear.setValue(data.getCertificationDate());
				dateYear.setWidth("100%");
				dateYear.setFormat(Labels.getLabel("common.dateFormatddMMMyyyy"));
				dateYear.addEventListener("onChange", new EventListener<Event>() {
					@Override
					public void onEvent(Event arg0) throws Exception {
						data.setCertificationDate(((Datebox) arg0.getTarget()).getValue());
						data.setIsHasChange(true);
						setHasChanged(true);
					}
				});

				lclDateYear.appendChild(dateYear);
				list.appendChild(lclDateYear);

			}
		});
	}

	private void setItemRenderFileCertificates() {
		// document file
		lsbAttachmentCertificates.setItemRenderer(new ListitemRenderer<DocumentITMS>() {
			@Override
			public void render(Listitem item, DocumentITMS data, int index) throws Exception {
				Listcell checkListcell = new Listcell();
				item.appendChild(checkListcell);

				Listcell nameListcell = new Listcell();
				Label nameLabel = new Label();
				if (null != data.getDocumentName()) {
					nameLabel.setValue(data.getDocumentName());
				}
				nameListcell.appendChild(nameLabel);
				item.appendChild(nameListcell);
			}
		});
	}

	@Listen("onOpenbdbLob = #lstboxRelatedLOB")
	public void onOpenbdbLobOnOpen(Event event) {
		EmployeeLob data = (EmployeeLob) event.getData();
		Map<String, Object> passedListener = new HashMap<String, Object>();
		passedListener.put("onSelect", doSelectLob(data));
		passedListener.put("onDeselect", doDeSelectLob(data));
		Executions.createComponents(ITMSPages.TOV_LINE_OF_BUSINESS.getUrl(), getSelf(), passedListener);
	}

	private EventListener<Event> doSelectLob(final EmployeeLob data) {

		return new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				LineOfBusiness selectedLob = (LineOfBusiness) event.getData();
				RegisterOutsourceEmployeeViewModel viewModel = new RegisterOutsourceEmployeeViewModel();
				mapComponentsToViewModel(viewModel);

				boolean exsist = false;
				boolean unique = false;
				for (EmployeeLob dataListView : viewModel.getLstboxRelatedLOB()) {
					if (null != dataListView.getLobId()
							&& dataListView.getLobId().longValue() == selectedLob.getLobId().longValue()
							&& dataListView.getOutEmployeeLobsId() != -1L) {
						exsist = true;
					} else if (null != dataListView.getLobId()
							&& dataListView.getLobId().longValue() == selectedLob.getLobId().longValue()
							&& dataListView.getOutEmployeeLobsId() == -1L) {
						unique = true;
					}

					if (exsist == true) {
						break;
					}
					if (unique == true) {
						break;
					}
				}
				// COMPETENCY MUST BE UNIQUE || ALREADY EXIST
				if (exsist == true) {
					MessagePopupUtils.error("LOB " + selectedLob.getLobName() + " already exist", null);
					
				} else if (unique == true) {
					MessagePopupUtils.error("LOB Name must be unique", null);
				} else {
					data.setLobId(Long.valueOf(selectedLob.getLobId()));
					data.setLobNumber(selectedLob.getLobNumber());
					data.setLobName(selectedLob.getLobName());
					data.setHasChange(true);
					bindViewModelToComponents(viewModel);
					setHasChanged(true);
				}
			}
		};
	}

	private EventListener<Event> doDeSelectLob(final EmployeeLob data) {
		return new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				RegisterOutsourceEmployeeViewModel viewModel = new RegisterOutsourceEmployeeViewModel();
				mapComponentsToViewModel(viewModel);
				data.setLobId(-1L);
				data.setLobName(null);
				data.setLobNumber(null);
				data.setHasChange(true);
				bindViewModelToComponents(viewModel);
				setHasChanged(true);
			}
		};
	}

	@Listen("onOpenbdbFunction = #lstboxFunction")
	public void onOpenbdbFunctionOnOpen(Event event) {
		EmployeeFunction data = (EmployeeFunction) event.getData();
		Map<String, Object> passedListener = new HashMap<String, Object>();
		passedListener.put("onSelect", doSelectFunction(data));
		passedListener.put("onDeselect", doDeSelectFunction(data));
		Executions.createComponents(ITMSPages.TOV_FUNCTION.getUrl(), getSelf(), passedListener);
	}

	private EventListener<Event> doSelectFunction(final EmployeeFunction data) {

		return new EventListener<Event>() {
			Long currentFunctionId = data.getFunctionId();

			@Override
			public void onEvent(Event event) throws Exception {
				Function selectedFunction = (Function) event.getData();
				RegisterOutsourceEmployeeViewModel viewModel = new RegisterOutsourceEmployeeViewModel();
				mapComponentsToViewModel(viewModel);

				boolean exsist = false;
				boolean unique = false;
				for (EmployeeFunction dataListView : viewModel.getLstboxFunction()) {
					if (null != dataListView.getFunctionId()
							&& dataListView.getFunctionId().longValue() == selectedFunction.getFunctionId().longValue()
							&& dataListView.getOutEmployeeFunctionId() != -1L) {
						exsist = true;
					} else if (null != dataListView.getFunctionId()
							&& dataListView.getFunctionId().longValue() == selectedFunction.getFunctionId().longValue()
							&& dataListView.getOutEmployeeFunctionId() == -1L) {
						unique = true;
					}

					if (exsist == true) {
						break;
					}
					if (unique == true) {
						break;
					}
				}
				// COMPETENCY MUST BE UNIQUE || ALREADY EXIST
				if (exsist == true) {
					MessagePopupUtils.error("Function " + selectedFunction.getFunctionName() + " already exist", null);
					
				} else if (unique == true) {
					MessagePopupUtils.error("Function Name must be unique", null);
				} else {
					data.setFunctionId(selectedFunction.getFunctionId());
					data.setFunctionName(selectedFunction.getFunctionName());
					data.setFunctionNumber(selectedFunction.getFunctionNumber());
					data.setHasChange(true);
					bindViewModelToComponents(viewModel);
					setHasChanged(true);
				}
			}
		};
	}

	private EventListener<Event> doDeSelectFunction(final EmployeeFunction data) {
		return new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				RegisterOutsourceEmployeeViewModel viewModel = new RegisterOutsourceEmployeeViewModel();
				mapComponentsToViewModel(viewModel);
				data.setFunctionId(-1L);
				data.setFunctionName(null);
				data.setFunctionNumber(null);
				data.setHasChange(true);
				bindViewModelToComponents(viewModel);
				setHasChanged(true);
			}
		};
	}
	
	@Listen("onClick=#btnAddRowLob")
	public void btnAddRowLobOnClick() {
		RegisterOutsourceEmployeeViewModel viewModel = new RegisterOutsourceEmployeeViewModel();
		mapComponentsToViewModel(viewModel);
		EmployeeLob lineOfBusiness = new EmployeeLob();
		lineOfBusiness.setOutEmployeeLobsId(-1L);
		lineOfBusiness.setLobId(-1L);
		lineOfBusiness.setHasChange(true);
		viewModel.getLstboxRelatedLOB().add(lineOfBusiness);
		setHasChanged(true);
	}

	@Listen("onClick=#btnAddRowFunction")
	public void btnAddRowFunctionOnClick() {
		RegisterOutsourceEmployeeViewModel viewModel = new RegisterOutsourceEmployeeViewModel();
		mapComponentsToViewModel(viewModel);
		EmployeeFunction function = new EmployeeFunction();
		function.setOutEmployeeFunctionId(-1L);
		function.setFunctionId(-1L);
		function.setHasChange(true);
		viewModel.getLstboxFunction().add(function);
		setHasChanged(true);
	}

	@Listen("onClick=#btnAddRowCertifications")
	public void btnAddRowCertificationsOnClick() {
		RegisterOutsourceEmployeeViewModel viewModel = new RegisterOutsourceEmployeeViewModel();
		mapComponentsToViewModel(viewModel);
		EmployeeCertification employeeCertification = new EmployeeCertification();
		employeeCertification.setCertificationId(-1L);
		employeeCertification.setIsCertified(0);
		employeeCertification.setCertificationDate(LocalDate.now().toDate());
		employeeCertification.setIsHasChange(true);
		viewModel.getLstboxCertifications().add(employeeCertification);
		setHasChanged(true);
	}

	@Listen("onClick=#btnDeleteRowLob")
	public void btnDeleteRowLobOnClick() {
		Iterator<Listitem> selections = lstboxRelatedLOB.getSelectedItems().iterator();
		if (!selections.hasNext()) {
			MessagePopupUtils.errorNoSelectedRow(null, null);
			return;
		} else {
			MessagePopupUtils.confirmDelete(null, new EventListener<Event>() {
				@Override
				public void onEvent(Event arg0) throws Exception {
					doDeleteLob();
				}
			}, null);
		}
	}

	@Listen("onClick=#btnDeleteRowFunction")
	public void btnDeleteRowFunctionOnClick() {
		Iterator<Listitem> selections = lstboxFunction.getSelectedItems().iterator();
		if (!selections.hasNext()) {
			MessagePopupUtils.errorNoSelectedRow(null, null);
			return;
		} else {
			MessagePopupUtils.confirmDelete(null, new EventListener<Event>() {
				@Override
				public void onEvent(Event arg0) throws Exception {
					doDeleteFunction();
				}
			}, null);
		}
	}

	@Listen("onClick=#btnDeleteRowCertifications")
	public void btnDeleteRowCertificationsOnClick() {
		Iterator<Listitem> selections = lstboxCertifications.getSelectedItems().iterator();
		if (!selections.hasNext()) {
			MessagePopupUtils.errorNoSelectedRow(null, null);
			return;
		} else {
			MessagePopupUtils.confirmDelete(null, new EventListener<Event>() {
				@Override
				public void onEvent(Event arg0) throws Exception {
					doDeleteCertifications();
				}
			}, null);
		}
	}

	protected void doDeleteLob() {
		RegisterOutsourceEmployeeViewModel viewModel=new RegisterOutsourceEmployeeViewModel();
		mapComponentsToViewModel(viewModel);
		ListModelList<EmployeeLob> listModel=viewModel.getLstboxRelatedLOB();
		
		Iterator<Listitem> listSelection = lstboxRelatedLOB.getSelectedItems().iterator();
		List<Integer> listDeleteUI = new ArrayList<Integer>();
		
		while (listSelection.hasNext()) {
            Listitem item = listSelection.next();
            listDeleteUI.add(item.getIndex());
        }
		
		Utils.sortInteger(listDeleteUI, SortDirection.DESCENDING);
		for(int index : listDeleteUI){
			listModel.remove(index);
		}
		
		viewModel.setLstboxRelatedLOB(listModel);
		bindViewModelToComponents(viewModel);
		setHasChanged(true);
		
	}
	
	protected void doDeleteFunction() {
		RegisterOutsourceEmployeeViewModel viewModel=new RegisterOutsourceEmployeeViewModel();
		mapComponentsToViewModel(viewModel);
		ListModelList<EmployeeFunction> listModel=viewModel.getLstboxFunction();
		
		Iterator<Listitem> listSelection = lstboxFunction.getSelectedItems().iterator();
		List<Integer> listDeleteUI = new ArrayList<Integer>();
		
		while (listSelection.hasNext()) {
            Listitem item = listSelection.next();
            listDeleteUI.add(item.getIndex());
        }
		
		Utils.sortInteger(listDeleteUI, SortDirection.DESCENDING);
		for(int index : listDeleteUI){
			listModel.remove(index);
		}
		
		viewModel.setLstboxFunction(listModel);
		bindViewModelToComponents(viewModel);
		setHasChanged(true);
	}

	protected void doDeleteCertifications() {
		RegisterOutsourceEmployeeViewModel viewModel=new RegisterOutsourceEmployeeViewModel();
		mapComponentsToViewModel(viewModel);
		ListModelList<EmployeeCertification> listModel=viewModel.getLstboxCertifications();
		
		Iterator<Listitem> listSelection =lstboxCertifications.getSelectedItems().iterator();
		List<Integer> listDeleteUI = new ArrayList<Integer>();
		
		while (listSelection.hasNext()) {
            Listitem item = listSelection.next();
            listDeleteUI.add(item.getIndex());
        }
		
		Utils.sortInteger(listDeleteUI, SortDirection.DESCENDING);
		for(int index : listDeleteUI){
			listModel.remove(index);
		}
		
		viewModel.setLstboxCertifications(listModel);
		bindViewModelToComponents(viewModel);
		setHasChanged(true);
	}
	
	
	@Listen("onOpen=#bdbOrganization")
	public void bdbOrganizationOnClick() {
		Map<String, Object> arg = new HashMap<>();
		arg.put("onSelect", new EventListener<Event>() {

			@Override
			public void onEvent(Event arg0) throws Exception {

				OrganizationITMS org = (OrganizationITMS) arg0.getData();
				RegisterOutsourceEmployeeViewModel viewModel = new RegisterOutsourceEmployeeViewModel();
				mapComponentsToViewModel(viewModel);
				
				
				Long responsibilityId=0L;
				Long executorId=0L;
				if(Utils.getSessionUser() != null){
					responsibilityId = Utils.getSecurity().getResponsibilityId();
					executorId = Utils.getSessionUserId();
				}

				ServiceResult<OrganizationITMS> resultIsActiveOrganization=resourceService.getActiveOrganization(org.getOrganizationId(), responsibilityId, executorId);
				if(resultIsActiveOrganization.isSuccess()){
					OrganizationITMS organizationITMS=resultIsActiveOrganization.getResult();
					//TAMBAH DAN 
					if(organizationITMS==null){
						String error = "Organization is not company FIF";
						MessagePopupUtils.error(error, null);
					}else{
						viewModel.setTxtOrganizationId(org.getOrganizationId());
						viewModel.setBdbOrganization(org.getOrganizationName());
					}
				}else{
					MessagePopupUtils.error(resultIsActiveOrganization.getFirstErrorMessage(), null);
				}
				bindViewModelToComponents(viewModel);
			}
		});

		arg.put("onDeselect", new EventListener<Event>() {
			@Override
			public void onEvent(Event arg0) throws Exception {
				RegisterOutsourceEmployeeViewModel viewModel = new RegisterOutsourceEmployeeViewModel();
				mapComponentsToViewModel(viewModel);
				viewModel.setTxtOrganizationId(-1L);
				viewModel.setBdbOrganization("");
				bindViewModelToComponents(viewModel);
			}
		});
		Executions.createComponents(ITMSPages.TOV_ORGANIZATION.getUrl(), getSelf(), arg);
		setHasChanged(true);
	}

	@Listen("onOpen=#bdbSupervisorOne")
	public void bdbSupervisorOneOnClick() {
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("onSelect", new EventListener<Event>() {
			@Override
			public void onEvent(Event arg0) throws Exception {
				UserITMS resource = (UserITMS) arg0.getData();
				if (resource.getPersonId() != null) {
					RegisterOutsourceEmployeeViewModel viewModel = new RegisterOutsourceEmployeeViewModel();
					mapComponentsToViewModel(viewModel);
					viewModel.setTxtSupervisiorOneId(resource.getPersonId());
					viewModel.setBdbSupervisorOne(resource.getEmployeeName());
					bindViewModelToComponents(viewModel);
					setHasChanged(true);
				} else {
					MessagePopupUtils.error("Error code [2] TOV Resource : Person ID  is null", null);
				}
			}
		});

		arg.put("onDeselect", new EventListener<Event>() {
			@Override
			public void onEvent(Event arg0) throws Exception {
				RegisterOutsourceEmployeeViewModel viewModel = new RegisterOutsourceEmployeeViewModel();
				mapComponentsToViewModel(viewModel);
				viewModel.setTxtSupervisiorOneId(-1L);
				viewModel.setBdbSupervisorOne("");
				bindViewModelToComponents(viewModel);
				setHasChanged(true);
			}
		});
		arg.put("mode", "resource");
		Executions.createComponents(ITMSPages.TOV_RESOURCE.getUrl(), getSelf(), arg);
	}

	@Listen("onOpen=#bdbSupervisorTwo")
	public void bdbSupervisorTwoOnClick() {
		Map<String, Object> arg = new HashMap<String, Object>();
		arg.put("onSelect", new EventListener<Event>() {
			@Override
			public void onEvent(Event arg0) throws Exception {
				UserITMS resource = (UserITMS) arg0.getData();
				if (resource.getPersonId() != null) {
					RegisterOutsourceEmployeeViewModel viewModel = new RegisterOutsourceEmployeeViewModel();
					mapComponentsToViewModel(viewModel);
					viewModel.setTxtSupervisorTwoId(resource.getPersonId());
					viewModel.setBdbSupervisorTwo(resource.getEmployeeName());
					bindViewModelToComponents(viewModel);
					setHasChanged(true);
				} else {
					MessagePopupUtils.error("Error code [2] TOV Resource : Person ID  is null value", null);
				}
			}
		});

		arg.put("onDeselect", new EventListener<Event>() {
			@Override
			public void onEvent(Event arg0) throws Exception {
				RegisterOutsourceEmployeeViewModel viewModel = new RegisterOutsourceEmployeeViewModel();
				mapComponentsToViewModel(viewModel);
				viewModel.setTxtSupervisorTwoId(null);
				viewModel.setBdbSupervisorTwo("");
				bindViewModelToComponents(viewModel);
				setHasChanged(true);
			}
		});
		arg.put("mode", "resource");
		Executions.createComponents(ITMSPages.TOV_RESOURCE.getUrl(), getSelf(), arg);
		
	}

	@Listen("onOpen=#bdbEmployeeLocation")
	public void bdbEmployeeLocationOnClick() {
		Map<String, Object> arg = new HashMap<>();
		arg.put("onSelect", new EventListener<Event>() {

			@Override
			public void onEvent(Event arg0) throws Exception {

				EmployeeLocation empLocation = (EmployeeLocation) arg0.getData();
				RegisterOutsourceEmployeeViewModel viewModel = new RegisterOutsourceEmployeeViewModel();
				mapComponentsToViewModel(viewModel);
				viewModel.setTxtEmployeeLocationId(empLocation.getEmployeeLocationId());
				viewModel.setBdbEmployeeLocation(empLocation.getLocationName());
				bindViewModelToComponents(viewModel);
				setHasChanged(true);
			}
		});

		arg.put("onDeselect", new EventListener<Event>() {
			@Override
			public void onEvent(Event arg0) throws Exception {
				EmployeeLocation empLocation = (EmployeeLocation) arg0.getData();
				RegisterOutsourceEmployeeViewModel viewModel = new RegisterOutsourceEmployeeViewModel();
				mapComponentsToViewModel(viewModel);
				viewModel.setTxtEmployeeLocationId(-1L);
				viewModel.setBdbEmployeeLocation("");
				bindViewModelToComponents(viewModel);
				setHasChanged(true);
			}
		});
		Executions.createComponents(ITMSPages.TOV_EMPLOYEE_LOCATION.getUrl(), getSelf(), arg);
	}

	@Listen("onClick =#btnSave")
	public void btnSaveOnClick() {
		
		final RegisterOutsourceEmployeeViewModel viewModel=new RegisterOutsourceEmployeeViewModel();
		mapComponentsToViewModel(viewModel);

		MessagePopupUtils.confirmSave(null, new EventListener<Event>() {
			@Override
			public void onEvent(Event arg0) throws Exception {
			    preparedConfirmGeneratedScTicket(viewModel);
			}
		}, null);
	}

	@Listen("onCheck=#chkTerminate")
	public void chkTerminateOnChange(){
		RegisterOutsourceEmployeeViewModel viewModel=new RegisterOutsourceEmployeeViewModel();
		mapComponentsToViewModel(viewModel);
		viewModel.setChkTerminate(0L);
		viewModel.setTxtTerminatedDate(null);
		if(chkTerminate.isChecked()==true){
			viewModel.setChkTerminate(1L);
			viewModel.setTxtTerminatedDate(LocalDate.now().toDate());
		}
		setHasChanged(true);
	}
	
	public String getCurrentGender(Long genderId){
		return (GenderEnum.getGenderTypeValue(genderId).equals("%%")?"":GenderEnum.getGenderTypeValue(genderId));
	}
	public String getCurentBloodType(Long bloodId){
		return (BloodTypeEnum.getEmployeeTypeValue(bloodId).equals("%%")? "":BloodTypeEnum.getEmployeeTypeValue(bloodId));
	}
	
	private void preparedConfirmGeneratedScTicket(final RegisterOutsourceEmployeeViewModel viewModel) {
		
		if (validateComponent(viewModel)) {
			
			MessagePopupUtils.confirmSave(Labels.getLabel("common.confirmationSaveSecurityTick"), new EventListener<Event>() {
				@Override
				public void onEvent(Event arg0) throws Exception {
					viewModel.setTxtFlagCreateSecurityTicket("Y");
		    		saveRegisterOutsource(viewModel);
				}
			}, new EventListener<Event>() {
				@Override
				public void onEvent(Event arg0) throws Exception {
					viewModel.setTxtFlagCreateSecurityTicket("N");
		    		saveRegisterOutsource(viewModel);
				}
			});

		}
	}
	protected void saveRegisterOutsource(RegisterOutsourceEmployeeViewModel viewModel) {
		
			/** get flag security ticket*/
			String flagCreateSecurityTicket=viewModel.getTxtFlagCreateSecurityTicket();
	
			/** Out Employee */
			OutEmployee outEmployee=new OutEmployee();
			outEmployee.setOutEmployeeId(-1L);
			outEmployee.setNpo(viewModel.getTxtNpo());
			outEmployee.setFullName(viewModel.getTxtFullName());
			outEmployee.setSpv1PersonId(viewModel.getTxtSupervisiorOneId());
			outEmployee.setSpv2PersonId(viewModel.getTxtSupervisorTwoId());
			outEmployee.setOrganizationId(viewModel.getTxtOrganizationId());
			//inisialisasi default
			outEmployee.setRegSecurityTicketId(0L);
			outEmployee.setTremSecurityTicketId(0L);
	
			Long executorId=0L;
			Long responsibilityId=0L;
			if(Utils.getSessionUser() != null){
				responsibilityId = Utils.getSecurity().getResponsibilityId();
				executorId = Utils.getSessionUserId();
			}
			
			if(chkTerminate.isChecked()==true){
				outEmployee.setIsAutoTermination(1L);
				outEmployee.setTerminatedDate(LocalDate.now().toDate());
				outEmployee.setTerminatedBy(executorId);
			}else{
				outEmployee.setIsAutoTermination(0L);
				outEmployee.setTerminatedDate(null);
				outEmployee.setTerminatedBy(null);
			}
			outEmployee.setIsTerminated(0L);
			outEmployee.setLokerNumber(viewModel.getTxtLokerNumber());
			outEmployee.setEffectiveStartDate(viewModel.getTxtEffectiveStartDate());
			outEmployee.setEffectiveEndDate(viewModel.getEffectiveEndDate());
			outEmployee.setEmployeeLocationId(viewModel.getTxtEmployeeLocationId());
			outEmployee.setPersonUuId(null);
			outEmployee.setEmployeePhoto(viewModel.getTxtEmployeePhoto());
			outEmployee.setExtensionPhoto(viewModel.getTxtExt());
			
			/** personal employee*/
			EmployeePersonalInformation empPersonalInfo=new EmployeePersonalInformation();
			empPersonalInfo.setPhoneNumber(viewModel.getTxtPhoneNumber());
			empPersonalInfo.setEmail(viewModel.getTxtEmail());
			empPersonalInfo.setContactName(viewModel.getTxtEmergencyContactName());
			empPersonalInfo.setContactNumber(viewModel.getTxtEmergencyContactNumber());
			empPersonalInfo.setContactRelation(viewModel.getTxtEmergencyContRelation());;
			empPersonalInfo.setGender(getCurrentGender(viewModel.getCmbGenderId()));
			empPersonalInfo.setAddress(viewModel.getTxtAddress());
			empPersonalInfo.setDateOfBirth(viewModel.getTxtDateOfBirth());
			empPersonalInfo.setBloodType(getCurentBloodType(viewModel.getCmbBloodTypeId()));
			/** sub form list*/
			List<EmployeeLob> relatedLob=new ArrayList<>();
			if(viewModel.getLstboxRelatedLOB()!=null){
				relatedLob=viewModel.getLstboxRelatedLOB();
			}
			List<EmployeeFunction> employeeFunctions=new ArrayList<>();
			if(viewModel.getLstboxFunction()!=null){
				employeeFunctions=viewModel.getLstboxFunction();
			}
			
			List<EmployeeCertification> employeeCertifications=new ArrayList<>();
			if(viewModel.getLstboxCertifications()!=null){
				employeeCertifications=viewModel.getLstboxCertifications();
			}
			
			List<DocumentITMS> documentCertificates=viewModel.getDocumentModel();
			ServiceResult<HashMap<String, Object>> resultRegOutsource=resourceService.saveRegisterOutsource(outEmployee,
					empPersonalInfo,
					relatedLob,
					employeeFunctions,
					employeeCertifications,
					documentCertificates,
					flagCreateSecurityTicket,
					executorId,
					responsibilityId);
			if(resultRegOutsource.isSuccess()){
				
				HashMap<String, Object> outMap=resultRegOutsource.getResult();
				OutEmployee outEmployeeNew=new OutEmployee();
				/** Get Out Employee */
				outEmployeeNew=(OutEmployee) outMap.get("outEmployee");
				
				/** memindahkan gambar ke dalam folder server  */
				preparedMovedOutsourceImage(outEmployeeNew);
				
				/** Get ITSM Ticket */
				ITSMTicket ticket=new ITSMTicket();
				ticket=(ITSMTicket) outMap.get("ticket");
				
				/** result generated ticket success : true or false */
				boolean isTicketSuccess=(boolean) outMap.get("isTicketSuccess");
				
				/** KIRIM EMAIL KE SUPERVISOR DAN ITSM TICKET (Jika generated)*/
				preparedSendMailToSupervisorAndITSMTicket(outEmployeeNew,ticket,isTicketSuccess,Utils.getSecurity().getResponsibilityId(),Utils.getSessionUserId());
				
				try {
					String moduleName = "Register Outsource Employee";
					Tabs tabs = (Tabs) this.getSelf().getParent().getParent().getParent().getFellow("tabs");
					Tab tab = (Tab) tabs.getFellow("tab_" + moduleName.replaceAll(" ", ""));
					Events.postEvent("onRequestReload", tab, null);

					MessagePopupUtils.infoSaveSuccess(resultRegOutsource.getFirstErrorMessage(), null);
					
					Utils.detachSelectedPanel((Window) getSelf());
				} catch (Exception e) {
					e.printStackTrace();
					getSelf().detach();
				}
			}else{
				MessagePopupUtils.error(resultRegOutsource.getFirstErrorMessage(), null);
			}
	}

	private void preparedSendMailToSupervisorAndITSMTicket(final OutEmployee outEmployeeNew, 
			final ITSMTicket ticket,
			final boolean isTicketSuccess, 
			final Long responsibilityId, 
			final Long executorId) {
		
		final String serverAddress = Utils.getServerAddress(Executions.getCurrent());
		final EventQueue<Event> eventQueue = EventQueues.lookup("createTicketSendEmail", EventQueues.DESKTOP, true);
		eventQueue.subscribe(new EventListener<Event>() {
			
			@Override
			public void onEvent(Event evt) throws Exception {
				if(evt.getName().compareTo("evtCreateTicketSendEmail") == 0){
					try{
						System.out.println("event start");
						/**
						 * 
						 * MENGIRIM EMAIL KE SUPERVISOR 1  
						 * DAN SUPERVISOR 2 (Jika diisi)
						 * 
						 * */
						ServiceResult<Boolean> spv=resourceService.sendMailRegistrasiOutsourceToSVP(outEmployeeNew,serverAddress, responsibilityId, executorId);
						/** 
						 * 
						 *  JIKA MELAKUKAN GENERATED TICKET DAN SUCCESS 
						 *  MENGIRIM EMAIL Ke SUPERVISOR dengan 
						 *  Format ITSM Ticket
						 *  
						 */
						if(isTicketSuccess==true){
							ItsmTicketActions action=ItsmTicketActions.ASSIGN_BY_SERVICE_CATALOG;
							String remarks="Generated Security Ticket";
							ServiceResult<Boolean> svcEmail = itsmTicketService.emailCreateTicket(ticket, action, serverAddress, remarks, responsibilityId, executorId);
						}
						
						if (getPage() == null || getSelf() == null
								|| getSelf().getDesktop() == null
								|| !getSelf().getDesktop().isAlive()) {
							eventQueue.unsubscribe(this);
							return;
						}
					}catch(Exception e){
						try{
							if (getPage() == null || getSelf() == null
									|| getSelf().getDesktop() == null
									|| !getSelf().getDesktop().isAlive()) {
								eventQueue.unsubscribe(this);
								return;
							}
						}catch(Exception ex){
							return;
						}
					}
				}
			}
		}, true);
		eventQueue.publish(new Event("evtCreateTicketSendEmail"));
		
	}


	private void preparedMovedOutsourceImage(OutEmployee outEmployee) {
		if(outEmployee.getOutEmployeeId()!=null){
			RegisterOutsourceEmployeeViewModel viewModel=new RegisterOutsourceEmployeeViewModel();
			mapComponentsToViewModel(viewModel);
			String filenameWithoutExt=viewModel.getTxtEmployeePhoto();
			String ext=viewModel.getTxtExt();
			
			if(uploadedFile!=null && filenameWithoutExt.length()>0 && ext.length()>0){
				String replaceName=filenameWithoutExt.replace(filenameWithoutExt, outEmployee.getOutEmployeeId()+"").replace("-", "Out");
				String newFileName=replaceName+ext;
				String path = PathFileEnum.REGISTER_PHOTO_FILE.getName();
				if (FileUtils.saveFileToDisk(uploadedFile, path, newFileName, null, null)) {
					setHasChanged(false);
				} else {
					MessagePopupUtils.error(Labels.getLabel("sc.failedUpload"), null);
				}
			}
		}
	}

	private boolean validateComponent(RegisterOutsourceEmployeeViewModel viewModel) {
		boolean isValid = true;
		Utils.clearErrorContainer(errFullName);Utils.clearErrorContainer(errSupervisorOne);Utils.clearErrorContainer(errOrganization);Utils.clearErrorContainer(errEffectiveStartDate);Utils.clearErrorContainer(errEffectiveEndDate);
		if (null == viewModel.getTxtFullName() || viewModel.getTxtFullName().trim().length() == 0) {
			isValid = false;
			String error = "Full Name must be filled";
			Utils.showErrorUsingContainer(errFullName, error);
		}

		if (null == viewModel.getBdbSupervisorOne() || viewModel.getBdbSupervisorOne().trim().length() == 0) {
			isValid = false;
			String error = "Supervisor 1 must be filled";
			Utils.showErrorUsingContainer(errSupervisorOne, error);
		}

		if (null == viewModel.getBdbOrganization() || viewModel.getBdbOrganization().trim().length() == 0) {
			isValid = false;
			String error = "Organization must be filled";
			Utils.showErrorUsingContainer(errOrganization, error);
		}

		if (null == viewModel.getTxtEffectiveStartDate()) {
			isValid = false;
			String error = "Effective Start Date must be filled";
			Utils.showErrorUsingContainer(errEffectiveStartDate, error);
		}
		if (null == viewModel.getEffectiveEndDate()) {
			isValid = false;
			String error = "Effective End Date must be filled";
			Utils.showErrorUsingContainer(errEffectiveEndDate, error);
		} else if (viewModel.getTxtEffectiveStartDate() != null && viewModel.getEffectiveEndDate() != null) {
			if (viewModel.getTxtEffectiveStartDate().after(viewModel.getEffectiveEndDate())) {
				isValid = false;
				String error = "Effective End Date must be higher than or equal to Effective Start Date";
				MessagePopupUtils.error(error, null);
			}
		}

		if (errListComponentRegisterOutsource.getChildren() == null) {
			errListComponentRegisterOutsource.setVisible(false);
		}else{
			errListComponentRegisterOutsource.setVisible(true);
		}

		Utils.clearErrorContainer(errListComponentRegisterOutsource);
		// START CHECK LIST LOB
		List<EmployeeLob> lstLobForCheck = viewModel.getLstboxRelatedLOB();
		if (lstLobForCheck != null) {
			int size = lstLobForCheck.size();
			boolean hasError = false;
			for (int i = 0; i < size; i++) {
				StringBuffer errorMessage = new StringBuffer();
				hasError = false;
				EmployeeLob lineOfBusiness = lstLobForCheck.get(i);
				if (null == lineOfBusiness.getLobName() || lineOfBusiness.getLobName().trim().length() == 0) {
					errorMessage.append("LOB Name must be filled");
					hasError = true;
					isValid = false;
				}

				if (hasError) {
					String error = "LOB - Row " + (i + 1) + " : " + errorMessage;
					Utils.showErrorUsingContainer(errListComponentRegisterOutsource, error);
				}
			}
		}
		// START CHECK LIST FUNCTION
		List<EmployeeFunction> lstFunctionCheck = viewModel.getLstboxFunction();
		if (lstFunctionCheck != null) {
			int size = lstFunctionCheck.size();
			boolean hasError = false;
			for (int i = 0; i < size; i++) {
				StringBuffer errorMessage = new StringBuffer();
				hasError = false;
				EmployeeFunction lineOfFunction = lstFunctionCheck.get(i);
				if (null == lineOfFunction.getFunctionName() || lineOfFunction.getFunctionName().trim().length() == 0) {
					errorMessage.append("Function Name must be filled");
					hasError = true;
					isValid = false;
				}

				if (hasError) {
					String error = "Function - Row " + (i + 1) + " : " + errorMessage;
					Utils.showErrorUsingContainer(errListComponentRegisterOutsource, error);
				}
			}
		}

		// START CHECK LIST CERTIFICATIONS
		List<EmployeeCertification> lstCertificationCheck = viewModel.getLstboxCertifications();
		if (lstCertificationCheck != null) {
			int size = lstCertificationCheck.size();
			boolean hasError = false;
			for (int i = 0; i < size; i++) {
				StringBuffer errorMessage = new StringBuffer();
				hasError = false;
				EmployeeCertification employeeCertification = lstCertificationCheck.get(i);
				if (null == employeeCertification.getTrainingName() || employeeCertification.getTrainingName().trim().length() == 0) {
					errorMessage.append("Training Name must be filled");
					hasError = true;
					isValid = false;
				}
				
				if(null!=employeeCertification.getTrainingName() && employeeCertification.getTrainingName().trim().length()!=0){
					boolean isSameData=false;
					for (int j = 0; j < size; j++) {
						if(null!=lstCertificationCheck.get(j).getTrainingName() && lstCertificationCheck.get(j).getTrainingName().trim().equalsIgnoreCase(employeeCertification.getTrainingName().trim())){
							if(i!=j){
							  isSameData=true;
							}
							if(isSameData==true) break;
						}
						if(isSameData==true) break;
					}
					if(isSameData==true){
						if (hasError) {
							errorMessage.append(", ");
						}
						errorMessage.append("Training Name must be unique");
						hasError = true;
						isValid = false;
					}
				}

				if (null == employeeCertification.getCertificationDate()) {
					if (hasError) {
						errorMessage.append(", ");
					}
					errorMessage.append("Date must be filled");
					hasError = true;
					isValid = false;
				}

				if (hasError) {
					String error = "Certifications - Row " + (i + 1) + " : " + errorMessage;
					Utils.showErrorUsingContainer(errListComponentRegisterOutsource, error);
				}
			}
		}

		return isValid;
	}

	
	/** upload photo */
	@Listen("onUpload=#btnUpload")
	public void onUploadClick(UploadEvent event) throws Exception{
		uploadedFile = event.getMedia();
		if(uploadedFile instanceof org.zkoss.image.Image){
			 String extension = uploadedFile.getName().substring(uploadedFile.getName().lastIndexOf("."),uploadedFile.getName().length());
		     String acceptedDocumentType=".jpg, .png";
		     if (acceptedDocumentType.toLowerCase().contains(extension.toLowerCase())){
		    	if(!pics.getChildren().isEmpty()){
		    		pics.getChildren().clear();
		    	}
		    	String filenameWithoutExt = uploadedFile.getName().substring(0, uploadedFile.getName().lastIndexOf("."));
				String ext = uploadedFile.getName().substring(uploadedFile.getName().lastIndexOf("."));
				String timestamp = LocalDateTime.now().toString("yyyyMMddHHmmss");
				String newFileName = filenameWithoutExt + "_" + timestamp + ext;

				org.zkoss.zul.Image image = new org.zkoss.zul.Image();
				image.setContent((org.zkoss.image.Image) uploadedFile);
			    image.setWidth("120px");
			    image.setHeight("180px");
			    image.setParent(pics);
			    
			    txtEmployeePhoto.setValue(newFileName);
			    txtFileNameWithoutExt.setValue(filenameWithoutExt);
			    txtExt.setValue(ext);
			    
				setHasChanged(true);
		     }else{
		          MessagePopupUtils.error(Labels.getLabel("sc.invalidType"), null);
		     }
		}else{
			MessagePopupUtils.error(Labels.getLabel("sc.invalidType"), null);
		}
	}
	
	@Listen("onClick=#btnDeleteAttachmentCertificates")
	public void btnDeleteAttachmentContractFilesOnClick(){
		Iterator<Listitem> selections = lsbAttachmentCertificates.getSelectedItems().iterator();
		if(!selections.hasNext()){
			MessagePopupUtils.errorNoSelectedRow(null, null);
			return;
		}else{
			MessagePopupUtils.confirmDelete(null, new EventListener<Event>() {
				@Override
				public void onEvent(Event arg0) throws Exception {
					doDeleteAttachment();
				}
			}, null);
		}
	}

	protected void doDeleteAttachment() {
		RegisterOutsourceEmployeeViewModel viewModel = new RegisterOutsourceEmployeeViewModel();
		mapComponentsToViewModel(viewModel);

		ListModelList<DocumentITMS> model=viewModel.getDocumentModel();
		Iterator<Listitem> listSelection = lsbAttachmentCertificates.getSelectedItems().iterator();
		List<Integer> listDeleteUI = new ArrayList<>();
		
		while(listSelection.hasNext()){
			Listitem item = listSelection.next();
			listDeleteUI.add(item.getIndex());
		}
		Utils.sortInteger(listDeleteUI, SortDirection.DESCENDING);
		for(int index : listDeleteUI){
			model.remove(index);
		}
		viewModel.setDocumentModel(model);
		bindViewModelToComponents(viewModel);
		setHasChanged(true);
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Listen("onUpload =#btnFileuploadAttachmentCertificates")
	public void btnFileUploadOnClick(UploadEvent event) throws Exception {
		RegisterOutsourceEmployeeViewModel viewModel = new RegisterOutsourceEmployeeViewModel();
		mapComponentsToViewModel(viewModel);

		String acceptedDocumentType = ".jpg, .png, .pdf";
		String path = PathFileEnum.REGISTER_CERTIFICATES_FILE.getName();
		Media uploadedFile = event.getMedia();
		String extension = uploadedFile.getName().substring(uploadedFile.getName().lastIndexOf("."),
				uploadedFile.getName().length());

		ListModelList<EmployeeCertificates> listModelListUpload = (ListModelList) lsbAttachmentCertificates.getModel();
		if (lsbAttachmentCertificates != null || lsbAttachmentCertificates.getItemCount() <= 0) {
			listModelListUpload = (ListModelList) lsbAttachmentCertificates.getModel();
		}
		if (acceptedDocumentType.toLowerCase().contains(extension.toLowerCase())) {
			String filenameWithoutExt = uploadedFile.getName().substring(0, uploadedFile.getName().lastIndexOf("."));
			String ext = uploadedFile.getName().substring(uploadedFile.getName().lastIndexOf("."));
			String timestamp = LocalDateTime.now().toString("yyyyMMddHHmmss");
			String newFileName = filenameWithoutExt + "_" + timestamp + ext;
			if (FileUtils.saveFileToDisk(uploadedFile, path, newFileName, null, null)) {
				DocumentITMS doc = new DocumentITMS();
				doc.setDocumentName(newFileName);
				doc.setDocumentPath(path);
				
				viewModel.getDocumentModel().setMultiple(true);
				viewModel.getDocumentModel().add(doc);
				setHasChanged(true);
			} else {
				MessagePopupUtils.error(Labels.getLabel("sc.failedUpload"), null);
			}
		} else {
			MessagePopupUtils.error(Labels.getLabel("sc.invalidType"), null);
		}
	}
	
	private void setVisibleSubForm() {
		showHidePersonalInformation.setVisible(true);
		visibleRetaledLob.setVisible(false);
		visibleFunction.setVisible(false);
		visibleCertifications.setVisible(false);
//		visibleOutsourceCertificateFile.setVisible(false);
	}
	
	@Listen("onSelect=#lstboxSubFormResource")
	public void lstboxSubFormResourceOnChange(){

		RegisterOutsourceEmployeeViewModel viewModel=new RegisterOutsourceEmployeeViewModel();
		mapComponentsToViewModel(viewModel);
		setVisibleSubForm();
		showHidePersonalInformation.setVisible(false);
		String selected=SubFormRegsiterOutsourceEnum.getSubFormResourceValue(viewModel.getLstboxSubFormResourceId());
		if(selected.equals(SubFormRegsiterOutsourceEnum.PERSONAL_INFORMATION.getName())){
			showHidePersonalInformation.setVisible(true);
		}else if(selected.equals(SubFormRegsiterOutsourceEnum.RELATED_LOB.getName())){
			visibleRetaledLob.setVisible(true);
		}else if(selected.equals(SubFormRegsiterOutsourceEnum.FUNCTION.getName())){
			visibleFunction.setVisible(true);
		}else if(selected.equals(SubFormRegsiterOutsourceEnum.CERTIFICATIONS.getName())){
			visibleCertifications.setVisible(true);
		}
	}
	  
	@Listen("onChange = #txtNpo; "
		  + "onChange = #txtFullName; "
		  + "onChange = #txtEffectiveStartDate;"
		  + "onChange = #effectiveEndDate;"
		  + "onChange = #txtLokerNumber;"
		  + "onChange = #txtPhoneNumber;"
		  + "onChange = #txtEmail;"
		  + "onChange = #txtEmergencyContactName;"
		  + "onChange = #txtEmergencyContactNumber;"
		  + "onChange = #txtEmergencyContRelation;"
		  + "onChange = #txtAddress;"
		  + "onChange = #txtDateOfBirth;"
		  + "onSelect = #cmbGender;"
		  + "onSelect = #cmbBloodType;")
	public void onChangeComponent() {
		setHasChanged(true);
	}
	@Listen("onClick = #btnCancel")
	public void btnCancelOnClick(Event event) {
		if (hasChanged()) {
			MessagePopupUtils.confirmUnsavedData(null, new EventListener<Event>() {
				@Override
				public void onEvent(Event arg0) throws Exception {
					Utils.detachSelectedPanel((Window) getSelf());
				}
			}, null);
		} else {
			MessagePopupUtils.confirmCancel(null, new EventListener<Event>() {
				@Override
				public void onEvent(Event arg0) throws Exception {
					Utils.detachSelectedPanel((Window) getSelf());
				}
			}, null);
		}
	}
	
	public void cancel() {
		try {
			String moduleName = "Register Outsource Employee";
			Tabs tabs = (Tabs) this.getSelf().getParent().getParent().getParent().getFellow("tabs");
			Tab tab = (Tab) tabs.getFellow("tab_" + moduleName.replaceAll(" ", ""));
			Events.postEvent("onRequestReload", tab, null);
			Utils.detachSelectedPanel((Window) getSelf());
		} catch (Exception e) {
			e.printStackTrace();
			getSelf().detach();
		}
	}
	
	@Override
	protected FormMode getFormMode() {
		return null;
	}

	@Override
	protected List<String> getRolesOrResponsibilitiesOrFieldPermissionsNames() {
		return null;
	}
}
