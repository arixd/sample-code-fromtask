package id.co.fifgroup.fifgroup_rm.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.time.DateFormatUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.image.AImage;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SerializableEventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.A;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Button;
import org.zkoss.zul.Caption;
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
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Row;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabs;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import id.co.fifgroup.fifgroup_itms.global.annotation.PermissionMapping;
import id.co.fifgroup.fifgroup_itms.global.annotation.PermissionMappings;
import id.co.fifgroup.fifgroup_itms.global.annotation.PermissionMapping.Permission;
import id.co.fifgroup.fifgroup_itms.global.constant.FormMode;
import id.co.fifgroup.fifgroup_itms.global.constant.ITMSPages;
import id.co.fifgroup.fifgroup_itms.global.constant.ITSMPages;
import id.co.fifgroup.fifgroup_itms.global.constant.ItsmTicketActions;
import id.co.fifgroup.fifgroup_itms.global.constant.MonthEnum;
import id.co.fifgroup.fifgroup_itms.global.constant.Roles;
import id.co.fifgroup.fifgroup_itms.global.controller.BaseFormComposer;
import id.co.fifgroup.fifgroup_itms.global.model.Contract;
import id.co.fifgroup.fifgroup_itms.global.model.DocumentITMS;
import id.co.fifgroup.fifgroup_itms.global.model.EmployeeLocation;
import id.co.fifgroup.fifgroup_itms.global.model.Function;
import id.co.fifgroup.fifgroup_itms.global.model.ITMSNotificationTemplate;
import id.co.fifgroup.fifgroup_itms.global.model.IdValue;
import id.co.fifgroup.fifgroup_itms.global.model.LineOfBusiness;
import id.co.fifgroup.fifgroup_itms.global.model.OrganizationITMS;
import id.co.fifgroup.fifgroup_itms.global.model.RMCompetency;
import id.co.fifgroup.fifgroup_itms.global.model.StandardCompetency;
import id.co.fifgroup.fifgroup_itms.global.model.UserITMS;
import id.co.fifgroup.fifgroup_itms.global.service.NotificationManagerService;
import id.co.fifgroup.fifgroup_itms.global.service.RoleResponsibilityMappingService;
import id.co.fifgroup.fifgroup_itms.util.ComponentUtils;
import id.co.fifgroup.fifgroup_itms.util.CustomDateUtils;
import id.co.fifgroup.fifgroup_itms.util.CustomException;
import id.co.fifgroup.fifgroup_itms.util.FileUtils;
import id.co.fifgroup.fifgroup_itms.util.MessagePopupUtils;
import id.co.fifgroup.fifgroup_itms.util.ServiceResult;
import id.co.fifgroup.fifgroup_itms.util.Utils;
import id.co.fifgroup.fifgroup_itms.util.Utils.SortDirection;
import id.co.fifgroup.fifgroup_itsm.service.ITSMTicketService;
import id.co.fifgroup.fifgroup_rm.constanta.BloodTypeEnum;
import id.co.fifgroup.fifgroup_rm.constanta.GenderEnum;
import id.co.fifgroup.fifgroup_rm.constanta.MonthlyWorksheetStatusEnum;
import id.co.fifgroup.fifgroup_rm.constanta.PathFileEnum;
import id.co.fifgroup.fifgroup_rm.constanta.SubFormRegsiterOutsourceEnum;
import id.co.fifgroup.fifgroup_rm.model.CompetencyBasedFunction;
import id.co.fifgroup.fifgroup_rm.model.EmployeeCertificates;
import id.co.fifgroup.fifgroup_rm.model.EmployeeCertification;
import id.co.fifgroup.fifgroup_rm.model.EmployeeCompetency;
import id.co.fifgroup.fifgroup_rm.model.EmployeeFunction;
import id.co.fifgroup.fifgroup_rm.model.EmployeeLob;
import id.co.fifgroup.fifgroup_rm.model.EmployeePersonalInformation;
import id.co.fifgroup.fifgroup_rm.model.MonthlyWorksheetNotification;
import id.co.fifgroup.fifgroup_rm.model.OutEmployee;
import id.co.fifgroup.fifgroup_rm.service.ResourceService;
import id.co.fifgroup.fifgroup_rm.viewmodel.ReviewOutsourceEmployeeViewModel;

@SuppressWarnings({ "rawtypes", "unchecked" })
@VariableResolver(DelegatingVariableResolver.class)
public class ReviewOutsourceEmployeeComposer extends BaseFormComposer{
	private static final long serialVersionUID = 6144126273699327136L;

	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"OUTSOURCE_EMPLOYEE" }, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.READ_ONLY)
                           })
	@Wire
	private Button btnBrowse;

	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"OUTSOURCE_EMPLOYEE" }, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.READ_ONLY)
	})
	@Wire
	private Textbox txtNpo;
	
	@Wire
	private Textbox txtEmployeeId;

	@Wire
	private Label txtVendorRelatedContract;
	
	@PermissionMappings({
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"OUTSOURCE_EMPLOYEE" }, permission = Permission.READ_ONLY)
		,@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.READ_ONLY)
	})
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
	
	@PermissionMappings({
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"OUTSOURCE_EMPLOYEE" }, permission = Permission.READ_ONLY)
	,@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.READ_ONLY),
	@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.READ_ONLY)
	})
	@Wire
	private Textbox txtLokerNumber;
	
	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.READ_ONLY)
	})
	@Wire
	private Datebox txtEffectiveStartDate;

	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.READ_ONLY)
	})
	@Wire
	private Datebox effectiveEndDate;
	
	@Wire Datebox txtTerminatedDate;
	
	@PermissionMappings({
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"OUTSOURCE_EMPLOYEE" }, permission = Permission.READ_ONLY)
	,@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.READ_ONLY),
	@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.READ_ONLY)
	})
	@Wire
	private Bandbox bdbOrganization;
	
	@PermissionMappings({
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"OUTSOURCE_EMPLOYEE" }, permission = Permission.READ_ONLY)
	,@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.READ_ONLY),
	@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.READ_ONLY)
	})
	@Wire
	private Bandbox bdbSupervisorOne;
	
	@PermissionMappings({
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"OUTSOURCE_EMPLOYEE" }, permission = Permission.READ_ONLY)
		,@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.READ_ONLY)
	})
	@Wire
	private Bandbox bdbSupervisorTwo;

	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"OUTSOURCE_EMPLOYEE" }, permission = Permission.READ_ONLY)
		,@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.READ_ONLY)
	})
	@Wire
	private Bandbox bdbEmployeeLocation;

	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"OUTSOURCE_EMPLOYEE" }, permission = Permission.READ_ONLY)
		,@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.READ_ONLY)
	})
	@Wire
	private Checkbox chkTerminate;

	@Wire
	private Listbox lstboxSubFormResource;

	
	/** general informations */
	@Wire
	private Grid showHidePersonalInformation;

	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"OUTSOURCE_EMPLOYEE" }, permission = Permission.READ_ONLY)
		,@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.READ_ONLY)
	})
	@Wire
	private Textbox txtEmail;

	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"OUTSOURCE_EMPLOYEE" }, permission = Permission.READ_ONLY)
		,@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.READ_ONLY)
	})
	@Wire
	private Textbox txtEmergencyContactName;

	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"OUTSOURCE_EMPLOYEE" }, permission = Permission.READ_ONLY)
		,@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.READ_ONLY)
	})
	@Wire
	private Textbox txtAddress;

	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"OUTSOURCE_EMPLOYEE" }, permission = Permission.READ_ONLY)
		,@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.READ_ONLY)
	})
	@Wire
	private Textbox txtEmergencyContactNumber;

	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"OUTSOURCE_EMPLOYEE" }, permission = Permission.READ_ONLY)
		,@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.READ_ONLY)
	})
	@Wire
	private Textbox txtEmergencyContRelation;

	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"OUTSOURCE_EMPLOYEE" }, permission = Permission.READ_ONLY)
		,@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.READ_ONLY)
	})
	@Wire
	private Datebox txtDateOfBirth;

	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"OUTSOURCE_EMPLOYEE" }, permission = Permission.READ_ONLY)
		,@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.READ_ONLY)
	})
	@Wire
	private Listbox cmbGender;

	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"OUTSOURCE_EMPLOYEE" }, permission = Permission.READ_ONLY)
		,@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.READ_ONLY)
	})
	@Wire
	private Listbox cmbBloodType;

	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"OUTSOURCE_EMPLOYEE" }, permission = Permission.READ_ONLY)
		,@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.READ_ONLY)
	})
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

	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"OUTSOURCE_EMPLOYEE" }, permission = Permission.READ_ONLY)
		,@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.READ_ONLY)
	})
	@Wire
	private Listbox lstboxRelatedLOB;
	
	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"OUTSOURCE_EMPLOYEE" }, permission = Permission.HIDE)
		,@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.HIDE)
	})
	@Wire
	private Button btnAddRowLob;

	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"OUTSOURCE_EMPLOYEE" }, permission = Permission.HIDE)
		,@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.HIDE)
	})
	@Wire
	private Button btnDeleteRowLob;

	private List<Long> lstRemoveRelatedLOB = new ArrayList<>();

	/** listbox function */
	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"OUTSOURCE_EMPLOYEE" }, permission = Permission.READ_ONLY)
		,@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.READ_ONLY)
	})
	@Wire
	private Listbox lstboxFunction;

	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"OUTSOURCE_EMPLOYEE" }, permission = Permission.HIDE)
		,@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.HIDE)
	})
	@Wire
	private Button btnAddRowFunction;

	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"OUTSOURCE_EMPLOYEE" }, permission = Permission.HIDE)
		,@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.HIDE)
	})
	@Wire
	private Button btnDeleteRowFunction;

	private List<Long> lstRemoveFunction = new ArrayList<>();
	
	/** listbox certifications */
	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.READ_ONLY)
	})
	@Wire
	private Listbox lstboxCertifications;
	
	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.HIDE)
		,@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.HIDE)
	})
	@Wire
	private Button btnAddRowCertifications;

	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.HIDE)
	})
	@Wire
	private Button btnDeleteRowCertifications;

	private List<Long> lstRemoveCertificates = new ArrayList<>();
	
	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.HIDE)
	})
	@Wire
	private Button btnSave;

	/** Upload File Document */
	@Wire
	private Groupbox grbRegisterOutsourceFiles;
	
	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.HIDE)
	})
	@Wire
	private Fileupload btnFileuploadAttachmentCertificates;
	
	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.READ_ONLY)
	})
	@Wire
	private Listbox lsbAttachmentCertificates;

	private List<Long> lstRemoveAttachments = new ArrayList<>();
	
	private List<Long> lstRemoveCompetency=new ArrayList<>();
	
	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.HIDE)
	})
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

	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"OUTSOURCE_EMPLOYEE" }, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.READ_ONLY)
	})
	@Wire
	private Button btnUpload;
	@Wire
	private Caption captionTitle;
	/** group visible */
	@Wire 
	private Div visibleRetaledLob;
	@Wire 
	private Div visibleFunction;
	@Wire 
	private Div visibleCertifications;
//	@Wire 
//	private Div visibleOutsourceCertificateFile;
	@Wire
	private Div visibleCompetency;
	
	@PermissionMappings({ @PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"OUTSOURCE_EMPLOYEE" }, permission = Permission.HIDE)})
	@Wire
	private Div visibleRelatedContract;
	
	
	@PermissionMappings({	
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"OUTSOURCE_EMPLOYEE" }, permission = Permission.READ_ONLY)
		,@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.READ_ONLY)
	})
	@Wire
	private Listbox lstboxRelatedContract;
	
	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"OUTSOURCE_EMPLOYEE" }, permission = Permission.READ_ONLY)
		,@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.READ_ONLY),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.READ_ONLY)
	})
	@Wire
	private Listbox lstboxCompetencyBasdedOnFunction;
	
	@Wire
	private Listbox lstboxCompetency;
	
	@Wire
	private Listheader visibleSPVRating;
	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_MANAGEMENT_ADMIN"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.HIDE)
	})
	@Wire 
	private Button btnAddRowCompetency;
	
	@Wire
	private Row visibleRow;
	
	@Wire
	private Row settingRowTwo,settingRowThree,settingRowFour;
	
	@PermissionMappings({ 
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"VENDOR_MANAGEMENT"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"TERMINATED"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"DASHBORD"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.READ_ONLY, roleOrResponsibilityName = {"EMAIL"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_MANAGEMENT_ADMIN"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"IT_MANAGEMENT"}, permission = Permission.HIDE),
		@PermissionMapping(formMode = FormMode.EDIT, roleOrResponsibilityName = {"RESOURCE_STAFF"}, permission = Permission.HIDE)
	})
	@Wire 
	private Button btnDeleteRowCompetency;
	
	@Wire 
	private Textbox oldSupervisorOneId;
	
	@Wire 
	private Textbox oldSupervisorTwoId;
	
	@Wire 
	private Textbox flagSupervisorTwoHasChange;

	@Wire 
	private Textbox currentAssigneeOnSPVOne;
	
	@Wire 
	private Textbox currentAssigneeOnSPVTwo;
	
	private List<MonthlyWorksheetNotification> lstMonthlyWorksheet;
	
	@WireVariable(rewireOnActivate = true)
	private transient ITSMTicketService itsmTicketService;
	
	@WireVariable(rewireOnActivate=true)
	private transient ResourceService resourceService;

	@WireVariable(rewireOnActivate = true)
	private RoleResponsibilityMappingService roleRespMapService;
	
	@WireVariable(rewireOnActivate = true)
	private NotificationManagerService notificationManagerService;

	@WireVariable
	private HashMap<String, Object> arg;
	
	
	@Override
	protected void setDefaultField() {
		
		setDefaultAllComponentSubFormAndAllCmbbox();
		setDefaultDate();
		/**review outsource detail (Mengisi data)*/
		defaultDisplayDataOutsourceToComponent();
		/** set default all listbox sub form */
		setItemRenderFunctionEmployee();
		setItemRenderRelatedLOB();
		setItemRenderCertifications();
		setItemRenderCompetencyBasdedOnFunction();
		setItemRenderCompetency();
		setItemEmployeeReltedContract();
		setVisibleOnChangeSubForm();
	}

	public void defaultDisplayDataOutsourceToComponent(){
		Long outEmployeeId=Long.parseLong(arg.get("personId").toString());
		Long responsibilityId=0L;
		Long executorId=0L;
		
		if(null != Utils.getSecurity()){
			responsibilityId = Utils.getSecurity().getResponsibilityId();
			executorId = Utils.getSessionUserId();
		}
		
		if(outEmployeeId!=null){
			ServiceResult<OutEmployee> resultDataOutEmployee = resourceService.getActiveOutEmployeeById(outEmployeeId, responsibilityId, executorId);
			if (resultDataOutEmployee.isSuccess() && resultDataOutEmployee.getResult() !=null) {
				ReviewOutsourceEmployeeViewModel viewModel = new ReviewOutsourceEmployeeViewModel();
				mapComponentsToViewModel(viewModel);
				
				/**Display Employee Information*/
				OutEmployee outEmployee=resultDataOutEmployee.getResult();
				viewModel.setTxtOutEmployeeId(outEmployee.getOutEmployeeId());
				if(outEmployee.getNpo()!=null){
					txtNpo.setDisabled(true);
				}
				viewModel.setTxtNpo(outEmployee.getNpo());
				viewModel.setTxtFullName(outEmployee.getFullName());
				
				/** SET SUPERVISOR **/
				viewModel.setTxtSupervisiorOneId(outEmployee.getSpv1PersonId()==null?0L:outEmployee.getSpv1PersonId());
				viewModel.setBdbSupervisorOne(outEmployee.getSupervisorOneName()==null?"":outEmployee.getSupervisorOneName());

				viewModel.setTxtSupervisorTwoId(outEmployee.getSpv2PersonId()==null?0L:outEmployee.getSpv2PersonId());
				viewModel.setBdbSupervisorTwo(outEmployee.getSupervisorTwoName()==null?"":outEmployee.getSupervisorTwoName());
				
				/** SET OLD SPV1 AND SPV2 **/
				viewModel.setOldSupervisorOneId(outEmployee.getSpv1PersonId()==null?0L:outEmployee.getSpv1PersonId());
				viewModel.setOldSupervisorTwoId(outEmployee.getSpv2PersonId()==null?0L:outEmployee.getSpv2PersonId());
				
				viewModel.setTxtVendorRelatedContract(outEmployee.getVendorName()==null? "« based on related contract »":outEmployee.getVendorName());
				viewModel.setTxtOrganizationId(outEmployee.getOrganizationId());
				
				viewModel.setBdbOrganization(outEmployee.getOrganizationName());
				chkTerminate.setChecked(outEmployee.getIsAutoTermination()!=null && outEmployee.getIsAutoTermination()==1L?true:false);
				
				viewModel.setChkTerminate(outEmployee.getIsAutoTermination()!=null ? outEmployee.getIsAutoTermination():0L);
				viewModel.setTxtLokerNumber(outEmployee.getLokerNumber());
				viewModel.setTxtEffectiveStartDate(outEmployee.getEffectiveStartDate());
				viewModel.setEffectiveEndDate(outEmployee.getEffectiveEndDate());
				viewModel.setTxtEmployeeLocationId(outEmployee.getEmployeeLocationId());
				viewModel.setBdbEmployeeLocation(outEmployee.getLocationName());
				viewModel.setTxtEmployeePhoto(outEmployee.getEmployeePhoto());
				viewModel.setTxtLastUpdatedDate(outEmployee.getLastUpdateDate());

				viewModel.setTxtOrganizationId(outEmployee.getOrganizationId());
				viewModel.setBdbOrganization(outEmployee.getOrganizationName());
				// check Organization 
				ServiceResult<OrganizationITMS> resultIsActiveOrganization=resourceService.getActiveOrganization(outEmployee.getOrganizationId(), responsibilityId, executorId);
				if(resultIsActiveOrganization.isSuccess()){
					OrganizationITMS organizationOnHcms=resultIsActiveOrganization.getResult();
					if(organizationOnHcms==null){
						MessagePopupUtils.error("Organization "+outEmployee.getOrganizationName()+" is no longer valid", null);
					}
				}else{
					MessagePopupUtils.error(resultIsActiveOrganization.getFirstErrorMessage(), null);
				}
				
				/** Display Image : get path from db and display to zul */
				if(outEmployee.getEmployeePhoto()!=null){
				   String acceptedDocumentType=".jpg, .png";
				   String extension = outEmployee.getEmployeePhoto().substring(outEmployee.getEmployeePhoto().lastIndexOf("."),
							outEmployee.getEmployeePhoto().length());
				   if (acceptedDocumentType.toLowerCase().contains(extension.toLowerCase())){
					   /** if extension from db has been change */
					   org.zkoss.zul.Image imageComponent = new org.zkoss.zul.Image();
					   try {
						   AImage image=new AImage(outEmployee.getEmployeePhoto());
						   imageComponent.setContent(image);
						   imageComponent.setWidth("120px");
						   imageComponent.setHeight("180px");
						   imageComponent.setParent(pics);
					   } catch (IOException e) {
						   e.printStackTrace();
						   MessagePopupUtils.infoDeleteSuccess("File Image with name "+outEmployee.getEmployeePhoto().replace("/opt/fif/out_photo/","")+" Not Found ", null);
					   }
				   }else{
					   MessagePopupUtils.error(Labels.getLabel("sc.invalidType"), null);
				   }
				}
			    /** Display personal Information From db*/
				ServiceResult<EmployeePersonalInformation> resultDataPersonalInfo = resourceService.getOutEmployeeInformationById(outEmployeeId, responsibilityId, executorId);
				if(!resultDataPersonalInfo.isSuccess()){
					MessagePopupUtils.error(resultDataPersonalInfo.getFirstErrorMessage(), null);
				}
				EmployeePersonalInformation personalInfo =resultDataPersonalInfo.getResult();
				viewModel.setTxtPhoneNumber(personalInfo.getPhoneNumber());
				viewModel.setTxtEmail(personalInfo.getEmail());
				viewModel.setTxtEmergencyContactName(personalInfo.getContactName());
				viewModel.setTxtEmergencyContactNumber(personalInfo.getContactNumber());
				viewModel.setTxtEmergencyContRelation(personalInfo.getContactRelation());
				
				List<IdValue> cmbGenderType = GenderEnum.getAllGenderType();
				cmbGenderType.add(0, new IdValue(-1L, "- Please Select -"));
				viewModel.setCmbGender(new ListModelList<>(cmbGenderType));
				if(personalInfo.getGender()!=null){
					for (IdValue idValue : cmbGenderType) {
						if(personalInfo.getGender().equals(idValue.getName())){
							viewModel.setCmbGenderId(idValue.getId());
							break;
						}
					}
				}else{
					viewModel.setCmbGenderId(-1L);
				}
				
				viewModel.setTxtAddress(personalInfo.getAddress());
				viewModel.setTxtDateOfBirth(personalInfo.getDateOfBirth());
				
				List<IdValue> cmbBloodType = BloodTypeEnum.getAllBloodType();
				cmbBloodType.add(0, new IdValue(-1L, "- Please Select -"));
				viewModel.setCmbBloodType(new ListModelList<>(cmbBloodType));
				if(personalInfo.getBloodType()!=null){
					for (IdValue idValue : cmbBloodType) {
						if(personalInfo.getBloodType().equals(idValue.getName())){
							viewModel.setCmbBloodTypeId(idValue.getId());
							break;
						}
					}
				}else{
					viewModel.setCmbBloodTypeId(-1L);
				}
				
				// Get Data List Related LOB
				ServiceResult<List<EmployeeLob>> resultLOB=resourceService.getOutEmployeeLobById(outEmployeeId, responsibilityId, executorId);
				if(resultLOB.isSuccess()){
					ListModelList<EmployeeLob> modelList=new ListModelList<EmployeeLob>(resultLOB.getResult());
					modelList.setMultiple(true);
					viewModel.setLstboxRelatedLOB(modelList);
				}else{
					MessagePopupUtils.error(resultLOB.getFirstErrorMessage(), null);
				}
				
				/** Get  list data function*/
				ServiceResult<List<EmployeeFunction>> resultFunction=resourceService.getOutEmployeeFunctionById(outEmployeeId, responsibilityId, executorId);
				if(resultFunction.isSuccess()){
					ListModelList<EmployeeFunction> modelList=new ListModelList<EmployeeFunction>(resultFunction.getResult());
					modelList.setMultiple(true);
					viewModel.setLstboxFunction(modelList);
					
				}else{
					MessagePopupUtils.error(resultFunction.getFirstErrorMessage(), null);
				}
				
				/** Get list data certifications */
				ServiceResult<List<EmployeeCertification>> resultTraining=resourceService.getOutEmployeeCertificationById(outEmployeeId, responsibilityId, executorId);
				if(resultTraining.isSuccess()){
					ListModelList<EmployeeCertification> modelList=new ListModelList<EmployeeCertification>(resultTraining.getResult());
					modelList.setMultiple(true);
					viewModel.setLstboxCertifications(modelList);
				}else{
					MessagePopupUtils.error(resultTraining.getFirstErrorMessage(), null);
				}
				/** Get list data certificates*/
				ServiceResult<List<EmployeeCertificates>> resultAttacUpload=resourceService.getOutEmployeeCertificatesById(outEmployeeId, responsibilityId, executorId);
				if(resultAttacUpload.isSuccess()){
					List<EmployeeCertificates> fileUpload=resultAttacUpload.getResult();
					List<DocumentITMS> documents = new ArrayList<>();
					ListModelList<EmployeeCertificates> certificateModel=new ListModelList<>();
					
					for (int i = 0; i < resultAttacUpload.getResult().size(); i++) {
						DocumentITMS document=new DocumentITMS();
						Long documentITMSId=resultAttacUpload.getResult().get(i).getDocumentId();
						String documentITMSName=resultAttacUpload.getResult().get(i).getDocumentName();
						String documentITMSPath=resultAttacUpload.getResult().get(i).getDocumentPath();
						document.setDocumentId(documentITMSId);
						document.setDocumentPath(documentITMSPath);
						document.setDocumentName(documentITMSName);
						documents.add(i, document);
					}
					ListModelList<DocumentITMS> modelDoc = new ListModelList<>(documents);
					modelDoc.setMultiple(true);;
					viewModel.setDocumentModel(modelDoc);
				}else{
					MessagePopupUtils.error(resultTraining.getFirstErrorMessage(), null);
				}
				
				/**Get list data Competency based Employee*/
				ServiceResult<List<CompetencyBasedFunction>> resultCompetencyBasedEmp =resourceService.getCompetenciesBasedOnEmployeeByEmployeeId(outEmployeeId, responsibilityId, executorId);
				if(resultCompetencyBasedEmp.isSuccess()){
					List<CompetencyBasedFunction> competencyBasedFunction=resultCompetencyBasedEmp.getResult();
					ListModelList<CompetencyBasedFunction> modelList=new ListModelList<CompetencyBasedFunction>(competencyBasedFunction);
					viewModel.setLstboxCompetencyBasdedOnFunction(modelList);
				}else{
					MessagePopupUtils.error(resultCompetencyBasedEmp.getFirstErrorMessage(), null);
				}
				
				/**Get list data Employee Competency*/
				ServiceResult<List<EmployeeCompetency>> resultCompetencyEmployee =resourceService.getCompetenciesEmployeeByEmployeeId(outEmployeeId, responsibilityId, executorId);
				if(resultCompetencyEmployee.isSuccess()){
					List<EmployeeCompetency> employeeCompetency=resultCompetencyEmployee.getResult();
					ListModelList<EmployeeCompetency> modelList=new ListModelList<EmployeeCompetency>(employeeCompetency);
					modelList.setMultiple(true);
					viewModel.setLstboxCompetency(modelList);
				}else{
					MessagePopupUtils.error(resultCompetencyEmployee.getFirstErrorMessage(), null);
				}
				/** Get list data related Contract */
				ServiceResult<List<Contract>> resultContract=resourceService.getOutEmployeeRelatedContractById(outEmployeeId, responsibilityId, executorId);
				if(resultContract.isSuccess()){
					ListModelList<Contract> modelList=new ListModelList<Contract>(resultContract.getResult());
					modelList.setMultiple(true);
					viewModel.setLstboxRelatedContract(modelList);
				}else{
					MessagePopupUtils.error(resultTraining.getFirstErrorMessage(), null);
				}
				bindViewModelToComponents(viewModel);
				
			} else {
				MessagePopupUtils.error(resultDataOutEmployee.getFirstErrorMessage(), null);
			}
		}
	}

	
	private void setDefaultDate() {
		ReviewOutsourceEmployeeViewModel viewModel=new ReviewOutsourceEmployeeViewModel();
		mapComponentsToViewModel(viewModel);
		Date date = new Date();
		viewModel.setTxtEffectiveStartDate(LocalDate.now().toDate());
		viewModel.setEffectiveEndDate(LocalDate.now().toDate());
		bindViewModelToComponents(viewModel);
	}

	private void setDefaultAllComponentSubFormAndAllCmbbox() {
		ReviewOutsourceEmployeeViewModel viewModel = new ReviewOutsourceEmployeeViewModel();
		mapComponentsToViewModel(viewModel);
		// SET GROUPBOX PERSONAL INFORMATION
		viewModel.setChkTerminate(0L);
		viewModel.setTxtTerminatedDate(null);
		
		List<String> lstResponsibility = getRolesOrResponsibilitiesOrFieldPermissionsNames();
		boolean asOutsourceEmployee = false;
		for (String resp : lstResponsibility) {
			if (resp.equals("OUTSOURCE_EMPLOYEE")) {
				asOutsourceEmployee = true;
			}
		}
		if(asOutsourceEmployee==true){
			List<IdValue> cmbSubForm = SubFormRegsiterOutsourceEnum.getAllSubFormReviewOutsource();
			viewModel.setLstboxSubFormResource(new ListModelList<>(cmbSubForm));
			viewModel.setLstboxSubFormResourceId(1L);
		}else{
			List<IdValue> cmbSubForm = SubFormRegsiterOutsourceEnum.getAllSubFormReviewResource();
			viewModel.setLstboxSubFormResource(new ListModelList<>(cmbSubForm));
			viewModel.setLstboxSubFormResourceId(1L);
		}
		
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

		// SET GROUPBOX SUB FORM FUNCTION
		ListModelList<EmployeeFunction> modelListFunction = new ListModelList<EmployeeFunction>();
		modelListFunction.setMultiple(true);
		viewModel.setLstboxFunction(modelListFunction);

		// SET GROUPBOX SUB FORM RELATED LOB
		ListModelList<EmployeeLob> modelListLOB = new ListModelList<>();
		modelListLOB.setMultiple(true);
		viewModel.setLstboxRelatedLOB(modelListLOB);


		// SET GROUPBOX SUB FORM CERTIFICATED
		ListModelList<EmployeeCertification> modelListCertifications = new ListModelList<>();
		modelListCertifications.setMultiple(true);
		viewModel.setLstboxCertifications(modelListCertifications);
		
		//SET GROUP BOX ATTACHMENT
		ListModelList<DocumentITMS> modelListDocument=new ListModelList<>();
		modelListCertifications.setMultiple(true);
		viewModel.setDocumentModel(modelListDocument);
		
		// COMPETNCY BELUM
		
		//SET RELATED CONTRACT
		ListModelList<Contract> modelListRelatedContract=new ListModelList<>();
		modelListRelatedContract.setMultiple(true);
		viewModel.setLstboxRelatedContract(modelListRelatedContract);
		/**Very Important
		 * flag spv1 and spv2 ketika melakukan perubahan
		 * 
		 * **/
		viewModel.setFlagSupervisorOneHasChange("N");
		viewModel.setFlagSupervisorTwoHasChange("N");
		
		viewModel.setCurrentAssigneeOnSPVOne("N");
		viewModel.setCurrentAssigneeOnSPVTwo("N");
		
		setVisibleSubForm();
		bindViewModelToComponents(viewModel);
	}


	private void setVisibleOnChangeSubForm() {
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

				Listcell cllLobNumber = new Listcell(data.getLobNumber());
				list.appendChild(cllLobNumber);

				Listcell cllLobName = new Listcell();
				final Bandbox bdbLobName = new Bandbox();
				bdbLobName.setWidth("100%");
				bdbLobName.setReadonly(true);
				bdbLobName.addForward("onOpen", lstboxRelatedLOB, "onOpenbdbLob", data);
				bdbLobName.setValue(data.getLobName());
				cllLobName.appendChild(bdbLobName);
				list.appendChild(cllLobName);

				List<String> lstResponsibility = getRolesOrResponsibilitiesOrFieldPermissionsNames();
				Boolean asITManagement = Boolean.FALSE;
				Boolean asResourceSetup = Boolean.FALSE;
				Boolean asOutsourceEmployee = Boolean.FALSE;
				Boolean notifyDashbord = Boolean.FALSE;
				Boolean notifyEmail = Boolean.FALSE;
				Boolean isTerminatedData=Boolean.FALSE;
				Boolean asVendorManagement=Boolean.FALSE;
				
				for (String resp : lstResponsibility) {
					if (resp.equals("OUTSOURCE_EMPLOYEE")) {
						asOutsourceEmployee = Boolean.TRUE;
					}else if(resp.equals("RESOURCE_STAFF")){
						asResourceSetup= Boolean.TRUE;
					}else if(resp.equals("IT_MANAGEMENT")){
						asITManagement= Boolean.TRUE;
					}else if(resp.equals("DASHBORD")){
						notifyDashbord= Boolean.TRUE;
					}else if(resp.equals("EMAIL")){
						notifyEmail= Boolean.TRUE;
					}else if(resp.equals("VENDOR_MANAGEMENT")){
						asVendorManagement= Boolean.TRUE;
					}
					if(resp.equals("TERMINATED")){
						isTerminatedData=Boolean.TRUE;
					}
				}
				
				if(notifyEmail==Boolean.TRUE ||
						notifyDashbord==Boolean.TRUE ||
						asITManagement==Boolean.TRUE || 
						asOutsourceEmployee==Boolean.TRUE || 
						asResourceSetup==Boolean.TRUE ||
						isTerminatedData==Boolean.TRUE||
						asVendorManagement==Boolean.TRUE){
					bdbLobName.setDisabled(true);
				}
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

				Listcell cllFunctionNumber = new Listcell(data.getFunctionNumber());
				list.appendChild(cllFunctionNumber);

				Listcell cllFunctionName = new Listcell();
				final Bandbox bdbFunctionName = new Bandbox();
				bdbFunctionName.setWidth("100%");
				bdbFunctionName.setReadonly(true);
				bdbFunctionName.addForward("onOpen", lstboxFunction, "onOpenbdbFunction", data);
				bdbFunctionName.setValue(data.getFunctionName());
				cllFunctionName.appendChild(bdbFunctionName);
				list.appendChild(cllFunctionName);
				
				List<String> lstResponsibility = getRolesOrResponsibilitiesOrFieldPermissionsNames();
				Boolean asITManagement = Boolean.FALSE;
				Boolean asResourceSetup = Boolean.FALSE;
				Boolean asOutsourceEmployee = Boolean.FALSE;
				Boolean notifyDashbord = Boolean.FALSE;
				Boolean notifyEmail = Boolean.FALSE;
				Boolean isTerminatedData=Boolean.FALSE;
				Boolean asVendorManagement=Boolean.FALSE;
				
				for (String resp : lstResponsibility) {
					if (resp.equals("OUTSOURCE_EMPLOYEE")) {
						asOutsourceEmployee = Boolean.TRUE;
					}else if(resp.equals("RESOURCE_STAFF")){
						asResourceSetup= Boolean.TRUE;
					}else if(resp.equals("IT_MANAGEMENT")){
						asITManagement= Boolean.TRUE;
					}else if(resp.equals("DASHBORD")){
						notifyDashbord = Boolean.TRUE;
					}else if(resp.equals("EMAIL")){
						notifyEmail = Boolean.TRUE;
					}else if(resp.equals("VENDOR_MANAGEMENT")){
						asVendorManagement = Boolean.TRUE;
					}
					if(resp.equals("TERMINATED")){
						isTerminatedData=Boolean.TRUE;
					}
				}
				
				if(notifyEmail==Boolean.TRUE ||
						notifyDashbord==Boolean.TRUE ||
						asITManagement==Boolean.TRUE || 
						asOutsourceEmployee==Boolean.TRUE || 
						asResourceSetup==Boolean.TRUE || 
						isTerminatedData==Boolean.TRUE ||
						asVendorManagement==Boolean.TRUE)
				{
					bdbFunctionName.setDisabled(true);
				}
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
						data.setIsHasChange(true);
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

				List<String> lstResponsibility = getRolesOrResponsibilitiesOrFieldPermissionsNames();
				Boolean asITManagement = Boolean.FALSE;
				Boolean asResourceSetup = Boolean.FALSE;
//				Boolean asOutsourceEmployee = Boolean.FALSE;
				Boolean notifyDashbord = Boolean.FALSE;
				Boolean notifyEmail = Boolean.FALSE;
				Boolean isTerminatedData=Boolean.FALSE;
				Boolean asVendorManagement=Boolean.FALSE;
				
				for (String resp : lstResponsibility) {
					if(resp.equals("IT_MANAGEMENT")){
						asITManagement= Boolean.TRUE;
					}else if(resp.equals("DASHBORD")){
						notifyDashbord= Boolean.TRUE;
					}else if(resp.equals("EMAIL")){
						notifyEmail= Boolean.TRUE;
					}else if(resp.equals("VENDOR_MANAGEMENT")){
						asVendorManagement= Boolean.TRUE;
					}
					if(resp.equals("TERMINATED")){
						isTerminatedData=Boolean.TRUE;
					}
				}
				
				if(notifyEmail==Boolean.TRUE ||
						notifyDashbord==Boolean.TRUE ||
						asITManagement==Boolean.TRUE || 
						asResourceSetup==Boolean.TRUE ||
						isTerminatedData==Boolean.TRUE ||
						asVendorManagement==Boolean.TRUE )
				{
					txtTrainingName.setDisabled(true);
					checkbox.setDisabled(true);
					dateYear.setDisabled(true);
				}
			}
		});
	}

	private void setItemRenderCompetencyBasdedOnFunction() {
		lstboxCompetencyBasdedOnFunction.setItemRenderer(new ListitemRenderer<CompetencyBasedFunction>() {

			@Override
			public void render(Listitem list, final CompetencyBasedFunction data, int index) throws Exception {

				Listcell cllno = new Listcell();
				Label lblNo = new Label();
				lblNo.setValue(index + 1 + "");
				cllno.appendChild(lblNo);
				list.appendChild(cllno);

			    Listcell functionNameCell = new Listcell(data.getFunctionName());
                list.appendChild(functionNameCell);
                
                List<String> lstResponsibility = getRolesOrResponsibilitiesOrFieldPermissionsNames();
				Boolean notifyDashbord = Boolean.FALSE;
				Boolean notifyEmail = Boolean.FALSE;

				for (String resp : lstResponsibility) {
					if(resp.equals("DASHBORD")){
						notifyDashbord= Boolean.TRUE;
					}else if(resp.equals("EMAIL")){
						notifyEmail= Boolean.TRUE;
					}
				}
				
				if(notifyEmail==Boolean.TRUE ||
						notifyDashbord==Boolean.TRUE){
					 Listcell detailListcell = new Listcell(data.getCompetencyName());
					 list.appendChild(detailListcell);
				}else{
					    Listcell detailListcell = new Listcell("");
		                A detailA = new A(data.getCompetencyName());
		                detailA.addForward("onClick", "lstboxCompetencyBasdedOnFunction", "onClickDetail", data);
		                detailA.setStyle("align: center;");
		                detailListcell.appendChild(detailA);
		                list.appendChild(detailListcell);
				}
                
                
			    Listcell standardCompetencyCell = new Listcell(data.getStandardCompetencyName());
                list.appendChild(standardCompetencyCell);
                
                Listcell currentLevelByEmployee=new Listcell(data.getCurrentLevelByEmployee());
                list.appendChild(currentLevelByEmployee);
                currentLevelByEmployee.setStyle("color:red;");

                if(data.getScore()!=null){
                	if(data.getScore().longValue()<=data.getScoreLevelEmployee().longValue()){
                		currentLevelByEmployee.setStyle("color:#009900;");
                	}
                }
                
                Listcell currentLevelBySPV=new Listcell(data.getCurrentLevelBySVP());
                list.appendChild(currentLevelBySPV);
                currentLevelBySPV.setStyle("color:red;");
                if(data.getScore()!=null){
                	if(data.getScore().longValue()<=data.getScoreLevelSVP().longValue()){
                		currentLevelBySPV.setStyle("color:#009900;");
                	}
                }
			}
		});
	}
	
	private void setItemRenderCompetency() {
		
		
		lstboxCompetency.setItemRenderer(new ListitemRenderer<EmployeeCompetency>() {
			@Override
			public void render(Listitem list, final EmployeeCompetency data, int index) throws Exception {
				
				Listcell cllCheckbox = new Listcell();
				list.appendChild(cllCheckbox);
				
                Listcell cllCompetencyName = new Listcell();
				final Bandbox bdbCompetencyName = new Bandbox();
				bdbCompetencyName.setWidth("100%");
				bdbCompetencyName.setReadonly(true);
				bdbCompetencyName.addForward("onOpen", lstboxCompetency, "onOpenbdbCompetency", data);
				bdbCompetencyName.setValue(data.getCompetencyName());
				cllCompetencyName.appendChild(bdbCompetencyName);
				list.appendChild(cllCompetencyName);
               
                Listcell cllEmpStandarCompencyRating = new Listcell();
				final Bandbox empStandardCompetencyRating= new Bandbox();
				empStandardCompetencyRating.setWidth("100%");
				empStandardCompetencyRating.setReadonly(true);
				empStandardCompetencyRating.addForward("onOpen", lstboxCompetency, "onOpenbdbStandardCompetency", data);
				empStandardCompetencyRating.setValue(data.getStandardCompetencyName());
				cllEmpStandarCompencyRating.appendChild(empStandardCompetencyRating);
				list.appendChild(cllEmpStandarCompencyRating);

				Listcell cllSpvStandarCompencyRating = new Listcell();
				final Bandbox spvStandardCompetencyRating= new Bandbox();
				spvStandardCompetencyRating.setWidth("100%");
				spvStandardCompetencyRating.setReadonly(true);
				spvStandardCompetencyRating.addForward("onOpen", lstboxCompetency, "onOpenbdbSpvStandardCompetency", data);
				spvStandardCompetencyRating.setValue(data.getSpvStandardCompetencyName());
				cllSpvStandarCompencyRating.appendChild(spvStandardCompetencyRating);
				list.appendChild(cllSpvStandarCompencyRating);
                
            	Listcell lastUpdateOnCell=new Listcell(data.getLastUpdateDate()==null?"":DateFormatUtils.format(data.getLastUpdateDate(), Labels.getLabel("fullDateTime"))+"");
				list.appendChild(lastUpdateOnCell);
				Listcell lastUpdateByCell=new Listcell(data.getUserLastUpdatedBy());
				list.appendChild(lastUpdateByCell);
			
				List<String> lstResponsibility = getRolesOrResponsibilitiesOrFieldPermissionsNames();
				Boolean asSupervisor = Boolean.FALSE;
				Boolean asITManagementAdmin = Boolean.FALSE;
				Boolean asITManagement = Boolean.FALSE;
				Boolean asResourceSetup = Boolean.FALSE;
				Boolean notifyDashbord = Boolean.FALSE;
				Boolean notifyEmail = Boolean.FALSE;
				Boolean isTerminatedData=Boolean.FALSE;
				Boolean asVendorManagement=Boolean.FALSE;
				
				for (String resp : lstResponsibility) {
					if (resp.equals("SUPERVISIOR")) {
						asSupervisor = Boolean.TRUE;
					}else if (resp.equals("RESOURCE_MANAGEMENT_ADMIN")) {
						asITManagementAdmin = Boolean.TRUE;
					} else if(resp.equals("RESOURCE_STAFF")){
						asResourceSetup= Boolean.TRUE;
					} else if(resp.equals("IT_MANAGEMENT")){
						asITManagement= Boolean.TRUE;
					}else if(resp.equals("DASHBORD")){
						notifyDashbord= Boolean.TRUE;
					}else if(resp.equals("EMAIL")){
						notifyEmail= Boolean.TRUE;
					}else if(resp.equals("VENDOR_MANAGEMENT")){
						asVendorManagement= Boolean.TRUE;
					}
					if(resp.equals("TERMINATED")){
						isTerminatedData=Boolean.TRUE;
					}
				}
				if(notifyEmail==Boolean.TRUE ||
						notifyDashbord==Boolean.TRUE ||
						asSupervisor==Boolean.TRUE ||
						isTerminatedData== Boolean.TRUE ||
						asVendorManagement== Boolean.TRUE){
					empStandardCompetencyRating.setDisabled(true);
				}
				
				if(notifyEmail==Boolean.TRUE ||
						notifyDashbord==Boolean.TRUE ||
						asITManagementAdmin==Boolean.TRUE || 
						asResourceSetup== Boolean.TRUE || 
						asITManagement==Boolean.TRUE ||
						isTerminatedData==Boolean.TRUE ||
						asVendorManagement== Boolean.TRUE){
					bdbCompetencyName.setDisabled(true);
					empStandardCompetencyRating.setDisabled(true);
					spvStandardCompetencyRating.setDisabled(true);
				}
			}
		});
	}
	
	@Listen("onOpenbdbCompetency=#lstboxCompetency")
	public void bdbCompetencyOnOpen(Event event) {
		EmployeeCompetency data = (EmployeeCompetency) event.getData();
		Map<String, Object> passedListener = new HashMap<String, Object>();
		passedListener.put("onSelect", doSelectCompetency(data));
		passedListener.put("onDeselect", doDeSelectCompetency(data));
		Executions.createComponents(ITMSPages.TOV_COMPETENCY.getUrl(), getSelf(), passedListener);
	}
	
	private EventListener<Event> doSelectCompetency(final EmployeeCompetency data) {

		return new EventListener<Event>() {
			Long currentCompetencyId=data.getCompetencyId();
			
			@Override
			public void onEvent(Event event) throws Exception {
				RMCompetency selectedCompetecy = (RMCompetency) event.getData();
				ReviewOutsourceEmployeeViewModel viewModel = new ReviewOutsourceEmployeeViewModel();
				mapComponentsToViewModel(viewModel);

				boolean exsist = false;
				boolean unique = false;
				for (EmployeeCompetency dataListView : viewModel.getLstboxCompetency()) {
					if (null != dataListView.getCompetencyId()
							&& dataListView.getCompetencyId().longValue() == selectedCompetecy.getCompetencyId().longValue()
							&& dataListView.getEmployeeCompetenciesId() != -1L
							&& currentCompetencyId != selectedCompetecy.getCompetencyId()) {
						exsist = true;
					} else if (null != dataListView.getCompetencyId()
							&& dataListView.getCompetencyId().longValue() == selectedCompetecy.getCompetencyId().longValue()
							&& dataListView.getEmployeeCompetenciesId() == -1L
							&& currentCompetencyId!=selectedCompetecy.getCompetencyId() ) {
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
					MessagePopupUtils.error("Competency " + selectedCompetecy.getCompetencyName() + " already exist", null);
					
				} else if (unique == true) {
					MessagePopupUtils.error("Competency Name must be unique", null);
				} else {
					data.setCompetencyId(Long.valueOf(selectedCompetecy.getCompetencyId()));
					data.setCompetencyName(selectedCompetecy.getCompetencyName());;
					data.setHasChange(true);
					bindViewModelToComponents(viewModel);
					setHasChanged(true);
				}
			}
		};
	}
	
	private EventListener<Event> doDeSelectCompetency(final EmployeeCompetency data) {
		return new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				ReviewOutsourceEmployeeViewModel viewModel = new ReviewOutsourceEmployeeViewModel();
				mapComponentsToViewModel(viewModel);
				data.setCompetencyId(-1L);
				data.setCompetencyName(null);
				data.setHasChange(true);
				bindViewModelToComponents(viewModel);
				setHasChanged(true);
			}
		};
	}

	
	@Listen("onOpenbdbStandardCompetency=#lstboxCompetency")
	public void bdbStandardCompetency(Event event) {
		EmployeeCompetency data = (EmployeeCompetency) event.getData();
		Map<String, Object> passedListener = new HashMap<String, Object>();
		passedListener.put("onSelect", doSelectStdCompetency(data));
		passedListener.put("onDeselect", doDeSelectStdCompetency(data));
		Executions.createComponents(ITMSPages.TOV_STANDARD_COMPETENCY.getUrl(), getSelf(), passedListener);
	}
	
	private EventListener<Event> doSelectStdCompetency(final EmployeeCompetency data) {

		return new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				StandardCompetency selectedCompetecy = (StandardCompetency) event.getData();
				ReviewOutsourceEmployeeViewModel viewModel = new ReviewOutsourceEmployeeViewModel();
				mapComponentsToViewModel(viewModel);
				/**EMPLOYEE_STANDARD_ID
				 * */
				data.setStandardCompetencyId(selectedCompetecy.getStdCompetencyId());
				data.setStandardCompetencyName(selectedCompetecy.getStdCompetencyName());
				data.setHasChange(true);
				bindViewModelToComponents(viewModel);
				setHasChanged(true);
			}
		};
	}
	
	
	private EventListener<Event> doDeSelectStdCompetency(final EmployeeCompetency data) {
		return new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				ReviewOutsourceEmployeeViewModel viewModel = new ReviewOutsourceEmployeeViewModel();
				mapComponentsToViewModel(viewModel);
				data.setStandardCompetencyId(-1L);
				data.setStandardCompetencyName(null);
				data.setHasChange(true);
				bindViewModelToComponents(viewModel);
				setHasChanged(true);
			}
		};
	}
	
	
	@Listen("onOpenbdbSpvStandardCompetency=#lstboxCompetency")
	public void bdbSpvStandardCompetencyOnOpen(Event event) {
		EmployeeCompetency data = (EmployeeCompetency) event.getData();
		Map<String, Object> passedListener = new HashMap<String, Object>();
		passedListener.put("onSelect", doSelectSpvStdCompetency(data));
		passedListener.put("onDeselect", doDeSelectSpvStdCompetency(data));
		Executions.createComponents(ITMSPages.TOV_STANDARD_COMPETENCY.getUrl(), getSelf(), passedListener);
	}
	
	
	private EventListener<Event> doDeSelectSpvStdCompetency(final EmployeeCompetency data) {
		return new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				ReviewOutsourceEmployeeViewModel viewModel = new ReviewOutsourceEmployeeViewModel();
				mapComponentsToViewModel(viewModel);
				data.setSpvStandardCompetencyId(-1L);
				data.setSpvStandardCompetencyName(null);
				data.setHasChange(true);
				bindViewModelToComponents(viewModel);
				setHasChanged(true);
			}
		};
	}
	
	private EventListener<Event> doSelectSpvStdCompetency(final EmployeeCompetency data) {

		return new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				StandardCompetency selectedCompetecy = (StandardCompetency) event.getData();
				ReviewOutsourceEmployeeViewModel viewModel = new ReviewOutsourceEmployeeViewModel();
				mapComponentsToViewModel(viewModel);
				/**SPV_STANDARD_ID
				 * */
				data.setSpvStandardCompetencyId(selectedCompetecy.getStdCompetencyId());
				data.setSpvStandardCompetencyName(selectedCompetecy.getStdCompetencyName());
				data.setHasChange(true);
				bindViewModelToComponents(viewModel);
				setHasChanged(true);
			}
		};
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
			
			Long lobId = data.getLobId();
			
			@Override
			public void onEvent(Event event) throws Exception {
				LineOfBusiness selectedLob = (LineOfBusiness) event.getData();
				ReviewOutsourceEmployeeViewModel viewModel = new ReviewOutsourceEmployeeViewModel();
				mapComponentsToViewModel(viewModel);

				boolean exsist = false;
				boolean unique = false;
				for (EmployeeLob dataListView : viewModel.getLstboxRelatedLOB()) {
					if (null != dataListView.getLobId()
							&& dataListView.getLobId().longValue() == selectedLob.getLobId().longValue()
							&& lobId!=selectedLob.getLobId().longValue()
							&& dataListView.getOutEmployeeLobsId() != -1L) {
						exsist = true;
					} else if (null != dataListView.getLobId()
							&& dataListView.getLobId().longValue() == selectedLob.getLobId().longValue()
						    && lobId!=selectedLob.getLobId().longValue()
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
				ReviewOutsourceEmployeeViewModel viewModel = new ReviewOutsourceEmployeeViewModel();
				mapComponentsToViewModel(viewModel);
				data.setOutEmployeeLobsId(-1L);
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
				ReviewOutsourceEmployeeViewModel viewModel = new ReviewOutsourceEmployeeViewModel();
				mapComponentsToViewModel(viewModel);

				boolean exsist = false;
				boolean unique = false;
				
				for (EmployeeFunction dataListView : viewModel.getLstboxFunction()) {
					if (null != dataListView.getFunctionId()
							&& dataListView.getFunctionId().longValue() == selectedFunction.getFunctionId().longValue()
							&& dataListView.getOutEmployeeFunctionId() != -1L 
							&& currentFunctionId!=selectedFunction.getFunctionId()) {
						exsist = true;
					} else if (null != dataListView.getFunctionId()
							&& dataListView.getFunctionId().longValue() == selectedFunction.getFunctionId().longValue()
							&& dataListView.getOutEmployeeFunctionId() == -1L
							&& currentFunctionId!=selectedFunction.getFunctionId()) {
						unique = true;
					}

					if (exsist == true) {
						break;
					}
					if (unique == true) {
						break;
					}
				}
				
				if (exsist == true) {
					MessagePopupUtils.error("Function " + selectedFunction.getFunctionName()+ " already exist", null);
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
				ReviewOutsourceEmployeeViewModel viewModel = new ReviewOutsourceEmployeeViewModel();
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
	
	private void setItemEmployeeReltedContract() {
		lstboxRelatedContract.setItemRenderer(new ListitemRenderer<Contract>() {

			@Override
			public void render(Listitem list, final Contract data, int index) throws Exception {

				Listcell cllno = new Listcell();
				Label lblNo = new Label();
				lblNo.setValue(index + 1 + "");
				cllno.appendChild(lblNo);
				list.appendChild(cllno);

			    Listcell contractNumberCell = new Listcell(data.getContractNumber());
                list.appendChild(contractNumberCell);

                List<String> lstResponsibility = getRolesOrResponsibilitiesOrFieldPermissionsNames();
				Boolean asResourceSetup = Boolean.FALSE;
				Boolean notifyDashbord = Boolean.FALSE;
				Boolean notifyEmail = Boolean.FALSE;
				
				for (String resp : lstResponsibility) {
					if(resp.equals("RESOURCE_STAFF")){
						asResourceSetup= Boolean.TRUE;
					}else if(resp.equals("DASHBORD")){
						notifyDashbord= Boolean.TRUE;
					}else if(resp.equals("EMAIL")){
						notifyEmail= Boolean.TRUE;
					}
					
				}
				
				if(asResourceSetup== Boolean.TRUE || notifyDashbord== Boolean.TRUE || notifyEmail== Boolean.TRUE){
					 Listcell contractNameCell = new Listcell(data.getContractName());
		             list.appendChild(contractNameCell);
				}else{
	                Listcell detailListcell = new Listcell("");
	                A detailA = new A(data.getContractName());
	                detailA.addForward("onClick", "lstboxRelatedContract", "onClickDetail", data);
	                detailA.setStyle("align: center;");
	                detailListcell.appendChild(detailA);
	                list.appendChild(detailListcell);
				}
                
				Listcell contractStartDateCell = new Listcell(data.getContractStartDate()==null? "":DateFormatUtils.format(data.getContractStartDate(), "dd-MMM-yyyy"));
                list.appendChild(contractStartDateCell);
                
                Listcell contractEndDateCell = new Listcell(data.getContractEndDate()==null? "":DateFormatUtils.format(data.getContractEndDate(), "dd-MMM-yyyy"));
                list.appendChild(contractEndDateCell);
			}
		});
	}
	
	@Listen("onClick=#btnAddRowLob")
	public void btnAddRowLobOnClick() {
		ReviewOutsourceEmployeeViewModel viewModel = new ReviewOutsourceEmployeeViewModel();
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
		ReviewOutsourceEmployeeViewModel viewModel = new ReviewOutsourceEmployeeViewModel();
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
		ReviewOutsourceEmployeeViewModel viewModel = new ReviewOutsourceEmployeeViewModel();
		mapComponentsToViewModel(viewModel);
		EmployeeCertification employeeCertification = new EmployeeCertification();
		employeeCertification.setCertificationId(-1L);
		employeeCertification.setIsCertified(0);
		employeeCertification.setCertificationDate(LocalDate.now().toDate());
		employeeCertification.setIsHasChange(true);
		viewModel.getLstboxCertifications().add(employeeCertification);
		setHasChanged(true);
	}
	
	@Listen("onClick=#btnAddRowCompetency")
	public void btnAddRowCompetencyOnClick() {
		ReviewOutsourceEmployeeViewModel viewModel = new ReviewOutsourceEmployeeViewModel();
		mapComponentsToViewModel(viewModel);
		EmployeeCompetency employeeCompetency= new EmployeeCompetency();
		employeeCompetency.setEmployeeCompetenciesId(-1L);
		employeeCompetency.setHasChange(true);
		viewModel.getLstboxCompetency().add(employeeCompetency);
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
	
	@Listen("onClick=#btnDeleteRowCompetency")
	public void btnDeleteRowCompetencyOnClick() {
		Iterator<Listitem> selections = lstboxCompetency.getSelectedItems().iterator();
		if (!selections.hasNext()) {
			MessagePopupUtils.errorNoSelectedRow(null, null);
			return;
		} else {
			MessagePopupUtils.confirmDelete(null, new EventListener<Event>() {
				@Override
				public void onEvent(Event arg0) throws Exception {
					doDeleteCompetency();
				}
			}, null);
		}
	}

	
	protected void doDeleteLob() {
		ReviewOutsourceEmployeeViewModel viewModel=new ReviewOutsourceEmployeeViewModel();
		mapComponentsToViewModel(viewModel);
		ListModelList<EmployeeLob> listModel=viewModel.getLstboxRelatedLOB();
		
		Iterator<Listitem> listSelection = lstboxRelatedLOB.getSelectedItems().iterator();
		List<Integer> listDeleteUI = new ArrayList<Integer>();
		
		while (listSelection.hasNext()) {
            Listitem item = listSelection.next();
            listDeleteUI.add(item.getIndex());
            lstRemoveRelatedLOB.add(listModel.get(item.getIndex()).getOutEmployeeLobsId());
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
		ReviewOutsourceEmployeeViewModel viewModel=new ReviewOutsourceEmployeeViewModel();
		mapComponentsToViewModel(viewModel);
		ListModelList<EmployeeFunction> listModel=viewModel.getLstboxFunction();
		
		Iterator<Listitem> listSelection = lstboxFunction.getSelectedItems().iterator();
		List<Integer> listDeleteUI = new ArrayList<Integer>();
		
		while (listSelection.hasNext()) {
            Listitem item = listSelection.next();
            listDeleteUI.add(item.getIndex());
            lstRemoveFunction.add(listModel.get(item.getIndex()).getOutEmployeeFunctionId());
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
		ReviewOutsourceEmployeeViewModel viewModel=new ReviewOutsourceEmployeeViewModel();
		mapComponentsToViewModel(viewModel);
		ListModelList<EmployeeCertification> listModel=viewModel.getLstboxCertifications();
		
		Iterator<Listitem> listSelection =lstboxCertifications.getSelectedItems().iterator();
		List<Integer> listDeleteUI = new ArrayList<Integer>();
		
		while (listSelection.hasNext()) {
            Listitem item = listSelection.next();
            listDeleteUI.add(item.getIndex());
            lstRemoveCertificates.add(listModel.get(item.getIndex()).getCertificationId());
        }
		
		Utils.sortInteger(listDeleteUI, SortDirection.DESCENDING);
		for(int index : listDeleteUI){
			listModel.remove(index);
		}
		
		viewModel.setLstboxCertifications(listModel);
		bindViewModelToComponents(viewModel);
		setHasChanged(true);
	}
	
	
	protected void doDeleteCompetency() {
		ReviewOutsourceEmployeeViewModel viewModel=new ReviewOutsourceEmployeeViewModel();
		mapComponentsToViewModel(viewModel);
		ListModelList<EmployeeCompetency> listModel=viewModel.getLstboxCompetency();
		
		Iterator<Listitem> listSelection =lstboxCompetency.getSelectedItems().iterator();
		List<Integer> listDeleteUI = new ArrayList<Integer>();
		
		while (listSelection.hasNext()) {
            Listitem item = listSelection.next();
            listDeleteUI.add(item.getIndex());
            lstRemoveCompetency.add(listModel.get(item.getIndex()).getEmployeeCompetenciesId());
        }
		
		Utils.sortInteger(listDeleteUI, SortDirection.DESCENDING);
		for(int index : listDeleteUI){
			listModel.remove(index);
		}
		
		viewModel.setLstboxCompetency(listModel);
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
				ReviewOutsourceEmployeeViewModel viewModel = new ReviewOutsourceEmployeeViewModel();
				mapComponentsToViewModel(viewModel);
				
				/** check has change by some one else 
				 * */
				Long responsibilityId=0L;
				Long executorId=0L;
				if(Utils.getSessionUser() != null){
					responsibilityId = Utils.getSecurity().getResponsibilityId();
					executorId = Utils.getSessionUserId();
				}

				ServiceResult<OrganizationITMS> resultIsActiveOrganization=resourceService.getActiveOrganization(org.getOrganizationId(), responsibilityId, executorId);
				if(resultIsActiveOrganization.isSuccess()){
					OrganizationITMS organizationITMS=resultIsActiveOrganization.getResult();
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
				ReviewOutsourceEmployeeViewModel viewModel = new ReviewOutsourceEmployeeViewModel();
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
					ReviewOutsourceEmployeeViewModel viewModel = new ReviewOutsourceEmployeeViewModel();
					mapComponentsToViewModel(viewModel);
					
					/** add flag Supervisor One has Changed Y / N*/
					Long employeeId=viewModel.getTxtOutEmployeeId();
					Long currentAssignedSupervisorId=Long.valueOf(viewModel.getOldSupervisorOneId());
					String currentStatus=MonthlyWorksheetStatusEnum.NEED_APPROVAL.getName();
					
					ServiceResult<List<MonthlyWorksheetNotification>>  resultMonthly=resourceService.getMonthlyWorksheetByPersonIdAndStatus(employeeId, 
							currentAssignedSupervisorId,
							currentStatus, 
							Utils.getSessionUserId());

					if(!resultMonthly.isSuccess()){
						MessagePopupUtils.error(resultMonthly.getFirstErrorMessage(), null);
					}
					
					/**cek supervosor telah berubah
					 * cek memiliki monthly worksheet yang berstatus need approval*/
					if(!viewModel.getTxtSupervisiorOneId().equals(resource.getPersonId()) 
							&& resultMonthly.getResult()!=null){
						
						viewModel.setFlagSupervisorOneHasChange("Y");
						viewModel.setCurrentAssigneeOnSPVOne("Y");
						
						lstMonthlyWorksheet=new ArrayList<>();
						lstMonthlyWorksheet.addAll(resultMonthly.getResult());

						System.out.println("s************** 1"+viewModel.getOldSupervisorOneId());
						System.out.println("s************** "+viewModel.getBdbSupervisorOne());
						
					}else{
						System.out.println("s************** 2"+viewModel.getOldSupervisorOneId());
						System.out.println("s************** "+viewModel.getBdbSupervisorOne());
						
						viewModel.setFlagSupervisorOneHasChange("N");
						viewModel.setCurrentAssigneeOnSPVOne("N");
					}
					
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
				ReviewOutsourceEmployeeViewModel viewModel = new ReviewOutsourceEmployeeViewModel();
				mapComponentsToViewModel(viewModel);
				viewModel.setTxtSupervisiorOneId(0L);
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
					ReviewOutsourceEmployeeViewModel viewModel = new ReviewOutsourceEmployeeViewModel();
					mapComponentsToViewModel(viewModel);

					/** add flag Supervisor Two has Changed Y / N*/
					Long employeeId=viewModel.getTxtOutEmployeeId();
					Long currentAssignedSupervisorId=Long.valueOf(viewModel.getOldSupervisorTwoId());
					String currentStatus=MonthlyWorksheetStatusEnum.NEED_APPROVAL.getName();
					
					ServiceResult<List<MonthlyWorksheetNotification>>  resultMonthly=resourceService.getMonthlyWorksheetByPersonIdAndStatus(employeeId, 
							currentAssignedSupervisorId,
							currentStatus, 
							Utils.getSessionUserId());
					
					if(!resultMonthly.isSuccess()){
						MessagePopupUtils.error(resultMonthly.getFirstErrorMessage(), null);
					}
					
					/**cek supervosor telah berubah
					 * cek memiliki monthly worksheet yang berstatus need approval*/
					if(!viewModel.getTxtSupervisorTwoId().equals(resource.getPersonId()) 
							&& resultMonthly.getResult()!=null){
						lstMonthlyWorksheet=new ArrayList<>();
						lstMonthlyWorksheet.addAll(resultMonthly.getResult());
						viewModel.setFlagSupervisorTwoHasChange("Y");
						viewModel.setCurrentAssigneeOnSPVTwo("Y");
						
						
					}else{
						viewModel.setFlagSupervisorTwoHasChange("N");
						viewModel.setCurrentAssigneeOnSPVTwo("N");
					}
					
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
				ReviewOutsourceEmployeeViewModel viewModel = new ReviewOutsourceEmployeeViewModel();
				mapComponentsToViewModel(viewModel);
				
				
				/** add flag Supervisor Two has Changed Y / N*/
				
				Long employeeId=viewModel.getTxtOutEmployeeId();
				Long currentAssignedSupervisorId=Long.valueOf(viewModel.getOldSupervisorTwoId());
				String currentStatus=MonthlyWorksheetStatusEnum.NEED_APPROVAL.getName();
				
				ServiceResult<List<MonthlyWorksheetNotification>>  resultMonthly=resourceService.getMonthlyWorksheetByPersonIdAndStatus(employeeId, 
						currentAssignedSupervisorId,
						currentStatus, 
						Utils.getSessionUserId());
				if(!resultMonthly.isSuccess()){
					MessagePopupUtils.error(resultMonthly.getFirstErrorMessage(), null);
				}
				
				/**
				 * ===============================================
				 * Penanganan Khusus
				 * ===============================================
				 * jika monthly worksheet sedang  ada  atas dirinya SPV2 
				 * yang ditandai resultMonthly.getResult()!=null
				 * tetapi di deselect
				 * 
				 * Status akan menjadi Approved
				 * CurrentAssignee ke prev yang terdahulu yaitu  
				 * OLD SUPERVISOR 2
				 * [ viewModel.getOldSupervisorTwoId() ]
				 * 
				 * */
				if(resultMonthly.getResult()!=null){
					viewModel.setFlagSupervisorTwoHasChange("Y");
					viewModel.setCurrentAssigneeOnSPVTwo("Y");
					lstMonthlyWorksheet=new ArrayList<>();
					lstMonthlyWorksheet.addAll(resultMonthly.getResult());
				}else{
					viewModel.setFlagSupervisorTwoHasChange("N");
					viewModel.setCurrentAssigneeOnSPVTwo("N");
				}
				viewModel.setTxtSupervisorTwoId(0L);
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
				ReviewOutsourceEmployeeViewModel viewModel = new ReviewOutsourceEmployeeViewModel();
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
				ReviewOutsourceEmployeeViewModel viewModel = new ReviewOutsourceEmployeeViewModel();
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
		
		final ReviewOutsourceEmployeeViewModel viewModel=new ReviewOutsourceEmployeeViewModel();
		mapComponentsToViewModel(viewModel);

		MessagePopupUtils.confirmSave(null, new EventListener<Event>() {
			@Override
			public void onEvent(Event arg0) throws Exception {
				if(validateComponent(viewModel)){
					saveReviewOutsource(viewModel);
				}
			}
		}, null);
	}

	@Listen("onCheck=#chkTerminate")
	public void chkTerminateOnChange(){
		ReviewOutsourceEmployeeViewModel viewModel=new ReviewOutsourceEmployeeViewModel();
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
	
	
	protected void saveReviewOutsource(ReviewOutsourceEmployeeViewModel viewModel) {
			/** Out Employee */
			Long executorId=0L;
			Long responsibilityId=0L;
			if(Utils.getSessionUser() != null){
				responsibilityId = Utils.getSecurity().getResponsibilityId();
				executorId = Utils.getSessionUserId();
			}
			
			OutEmployee outEmployee=new OutEmployee();
			outEmployee.setOutEmployeeId(viewModel.getTxtOutEmployeeId()); 
			if(txtNpo.isDisabled()==true){
				outEmployee.setNpo(null);
			}else{
				outEmployee.setNpo(viewModel.getTxtNpo());
			}
			outEmployee.setFullName(viewModel.getTxtFullName());
			outEmployee.setSpv1PersonId(viewModel.getTxtSupervisiorOneId());
			outEmployee.setSpv2PersonId(viewModel.getTxtSupervisorTwoId()==0L?null:viewModel.getTxtSupervisorTwoId());
			outEmployee.setOrganizationId(viewModel.getTxtOrganizationId());
			outEmployee.setRegSecurityTicketId(0L);
			outEmployee.setTremSecurityTicketId(0L);
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
			outEmployee.setLastUpdateDate(viewModel.getTxtLastUpdatedDate());
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
			List<EmployeeCompetency> employeeCompetencies=new ArrayList<>();
			if(viewModel.getLstboxCompetency()!=null){
				employeeCompetencies=viewModel.getLstboxCompetency();
				
			}
			/** end sub form list*/
			
			ServiceResult<OutEmployee> resultRegOutsource=resourceService.saveReviewOutsource(outEmployee,
					empPersonalInfo,
					relatedLob,
					employeeFunctions,
					employeeCertifications,
					documentCertificates,
					employeeCompetencies, 
					lstRemoveRelatedLOB,
					lstRemoveFunction,
					lstRemoveCertificates,
					lstRemoveAttachments,
					lstRemoveCompetency,
					executorId,
					responsibilityId);
			if(resultRegOutsource.isSuccess()){
				OutEmployee outEmployeeNew=resultRegOutsource.getResult();
				
				/** MOVE OUTSOURCE IMAGE TO SERVER **/
				preparedMovedOutsourceImage(outEmployeeNew);
				
				/***PREFARED SETING PARAMETER MONTHLYWORKSHEET
				 * 
				 *  FOR MOVED CURRENT STATUS AND CURRENT ASSIGNED 
				 *  JIKA SUPERVISOR DI  PERBAHARUI DI REVIEW OUTSOURCE
				 *  BEREFEK KE CURRENT STATUS MONTHLYWORKSHEET
				 *  
				 *   *******/
				Long employeeId=outEmployeeNew.getOutEmployeeId();
				Long executor=Utils.getSessionUserId();
				Long responsibility=Utils.getSecurity().getResponsibilityId();
				
				Long currentAssignedSupervisor = 0L;
				Long oldCurrentAssignedSupervisor=0L;
				String currentStatusTo = "";
				Boolean spvHasChanged=Boolean.FALSE;
				
				/***SUPERVISOR 1*/
				if(viewModel.getFlagSupervisorOneHasChange().equals("Y")){
					
					 currentAssignedSupervisor=viewModel.getTxtSupervisiorOneId();
					 currentStatusTo=MonthlyWorksheetStatusEnum.NEED_APPROVAL.getName();
					 spvHasChanged=Boolean.TRUE;
					 oldCurrentAssignedSupervisor=viewModel.getOldSupervisorOneId();
				}
				
				/***SUPERVISOR 2*/
				if(viewModel.getFlagSupervisorTwoHasChange().equals("Y")){
					/**Preparing Parameters */

					 /** 
					  * Special handling
					  * if the supervisor 2 deselect, whereaas current position  Monthly Worksheet
					  * is in self (Atas Supervisor tersebut)
					  * Maka di isi dengan supervisor 2 yang old / yang sebelumnya / spv yang di deselect itu
					  * untuk current status monthlyworksheet diisi Approval 
					  * 
					  * viewModel.getTxtSupervisorTwoId()==0L menandakan deselect 
					  * 
					  * **/
					 if(viewModel.getTxtSupervisorTwoId()==0L){
						 /** WARNING
						  *  CURRENT ASSIGNED SUPERVISOR, KE YANG DULU ***/
						 currentAssignedSupervisor=viewModel.getOldSupervisorTwoId();
						 currentStatusTo=MonthlyWorksheetStatusEnum.APPROVED.getName();
						 spvHasChanged=Boolean.TRUE;
						 oldCurrentAssignedSupervisor=viewModel.getOldSupervisorTwoId();
					 }else{
						 currentAssignedSupervisor=viewModel.getTxtSupervisorTwoId();
						 currentStatusTo=MonthlyWorksheetStatusEnum.NEED_APPROVAL.getName();
						 spvHasChanged=Boolean.TRUE;
						 
						 oldCurrentAssignedSupervisor=viewModel.getOldSupervisorTwoId();
					 }
				}
				
				if(spvHasChanged==Boolean.TRUE 
						&& currentAssignedSupervisor!=0L 
						&& currentStatusTo.trim().length()>0)
				{
					
					
					
					/** PREPARED SEND MAIL AND DASHBOARD MONTHLY WORKSHEET */
					/** 
					 *  Event Queue tidak dapat mengambil  Utils.getSessionUser().getEmployeeNumber()
					 *  Oleh karena itu harus megirimkan Utils.getSessionUser().getEmployeeNumber() dari composer
					 *  dan dikirim ke services
					 *  
					 *  */
					String userSender=Utils.getSessionUser().getEmployeeNumber().toString();

					
					preparedSendMailAndDashboardMonthlyWorkSheetByParam(lstMonthlyWorksheet,
							outEmployee,
							userSender,
							currentAssignedSupervisor,
							currentStatusTo,
							executor,
							responsibility);
					
					/** UPDATED MONTHLYWORKSHEET CURRENT ASIGNED DAN STATUS */
					ServiceResult<Integer> resultMonthlyWorksheetCurrentAssigneeAndStatus=resourceService.updatedMonthlyWorksheetCurrentAssigneeAndStatus(lstMonthlyWorksheet,employeeId,
							currentAssignedSupervisor,
							currentStatusTo,
							executorId,responsibilityId);
					if(!resultMonthlyWorksheetCurrentAssigneeAndStatus.isSuccess()){
						MessagePopupUtils.error(resultMonthlyWorksheetCurrentAssigneeAndStatus.getFirstErrorMessage(), null);
					}
					
					/** Information Email  */
					if(currentStatusTo.equals(MonthlyWorksheetStatusEnum.APPROVED.getName())){
						MessagePopupUtils.infoSaveSuccess("Data has been saved and Monthly Worksheets has approved", null);
					}else{
						MessagePopupUtils.infoSaveSuccess("Monthly Worksheets that need approval will be approved by the new supervisor ", null);
					}
				
				
				}else{
					MessagePopupUtils.infoSaveSuccess(null, null);
				}
				
				setHasChanged(false);
				cancel();
			}else{
				MessagePopupUtils.error(resultRegOutsource.getFirstErrorMessage(), null);
			}
	}
	//END TAHAP UJI COBA

	private void preparedSendMailAndDashboardMonthlyWorkSheetByParam(
			final List<MonthlyWorksheetNotification> lstMonthlyWorksheet,
			final OutEmployee employee,
			final String userSender,
			final Long currentAssignedSupervisor,
			final String currentStatusTo,
			final Long executorId,
			final Long responsibilityId) {
		
		ReviewOutsourceEmployeeViewModel viewModel=new ReviewOutsourceEmployeeViewModel();
		mapComponentsToViewModel(viewModel);
			
		final String serverAddress = Utils.getServerAddress(Executions.getCurrent());
		final Long currentAssignedSupervisorId=currentAssignedSupervisor;
		final EventQueue<Event> eventQueue = EventQueues.lookup("createTicketSendEmail", EventQueues.DESKTOP, true);
		
		eventQueue.subscribe(new EventListener<Event>() {
			
			@Override
			public void onEvent(Event evt) throws Exception {
				if(evt.getName().compareTo("evtCreateTicketSendEmail") == 0){
					try{
						System.out.println("event start");
						/**
						 * 
						 * MENGIRIM EMAIL DAN DASHBOARD  KE SUPERVISOR UNTUK SEMUA MONTHLY WORKSHEET
						 * YANG SEDANG APPROVAL 
						 * 
						 * */
						
						ServiceResult<ITMSNotificationTemplate> resultTemplateNeedApproval = notificationManagerService.getNotificationTemplate("NEED_APPROVAL", "NEED_REVISION", executorId);
						
						if(!resultTemplateNeedApproval.isSuccess()){
							throw new CustomException(resultTemplateNeedApproval.getFirstErrorMessage());
						}
						ITMSNotificationTemplate template=resultTemplateNeedApproval.getResult();
						
						if(template==null){
							throw new CustomException("Null value when get template need approval");
						}
						
						ServiceResult<Boolean> spv=resourceService.sendNeedApprovalNotifyAndMailToSVPByEmpAndStatus(
								lstMonthlyWorksheet,
								employee,
								userSender,
								currentAssignedSupervisorId,
								serverAddress,
								template,
								currentStatusTo, 
								executorId, 
								responsibilityId);

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
			ReviewOutsourceEmployeeViewModel viewModel=new ReviewOutsourceEmployeeViewModel();
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

	private boolean validateComponent(ReviewOutsourceEmployeeViewModel viewModel) {
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
		if (errListComponentRegisterOutsource.getChildren() == null) {errListComponentRegisterOutsource.setVisible(false);}else{errListComponentRegisterOutsource.setVisible(true);}
		
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
		
		/** Jika yang login supervisor maka supervisor rating must be field */
		/** Jika yang login outsource  maka employee rating must be field */
		List<String> lstResponsibility = getRolesOrResponsibilitiesOrFieldPermissionsNames();
		Boolean asSupervisor = Boolean.FALSE;
		Boolean asOutsourceEmployee = Boolean.FALSE;
		
		for (String resp : lstResponsibility) {
			if (resp.equals("SUPERVISIOR")) {
				asSupervisor = Boolean.TRUE;
			}else if(resp.equals("OUTSOURCE_EMPLOYEE")){
				asOutsourceEmployee= Boolean.TRUE;
			}
		}
		
		List<EmployeeCompetency> lstEmployeeCompetencyCheck=viewModel.getLstboxCompetency();
		if(lstEmployeeCompetencyCheck!=null){
			int size=lstEmployeeCompetencyCheck.size();
			boolean hasError=false;
			for (int i = 0; i < size; i++) {
				StringBuffer errorMessage = new StringBuffer();
				hasError = false;
				EmployeeCompetency employeeCompetency=lstEmployeeCompetencyCheck.get(i);
				if(null==employeeCompetency.getCompetencyName() || employeeCompetency.getCompetencyName().trim().length()==0){
					errorMessage.append("Competency Name must be filled");
					hasError = true;
					isValid = false;
				}
				
				if(asOutsourceEmployee==Boolean.TRUE){
					if(null==employeeCompetency.getStandardCompetencyName() || employeeCompetency.getStandardCompetencyName().trim().length()==0){
						if (hasError) {
							errorMessage.append(", ");
						}
						errorMessage.append("Employee's Rating must be filled");
						hasError = true;
						isValid = false;
					}
				}
				if(asSupervisor==Boolean.TRUE){
					if(null==employeeCompetency.getSpvStandardCompetencyName() || employeeCompetency.getSpvStandardCompetencyName().trim().length()==0){
						if (hasError) {
							errorMessage.append(", ");
						}
						errorMessage.append("Supervisor's Rating must be filled");
						hasError = true;
						isValid = false;
					}
				}
				if (hasError) {
					String error = "Competency - Row " + (i + 1) + " : " + errorMessage;
					Utils.showErrorUsingContainer(errListComponentRegisterOutsource, error);
				}
			}
		}
		
		Long responsibilityId=0L;
		Long executorId=0L;
		if(Utils.getSessionUser() != null){
			responsibilityId = Utils.getSecurity().getResponsibilityId();
			executorId = Utils.getSessionUserId();
		}

		ServiceResult<OrganizationITMS> resultIsActiveOrganization=resourceService.getActiveOrganization(viewModel.getTxtOrganizationId(), responsibilityId, executorId);
		if(resultIsActiveOrganization.isSuccess()){
			OrganizationITMS organizationITMS=resultIsActiveOrganization.getResult();
			if(organizationITMS==null && null != viewModel.getBdbOrganization() && viewModel.getBdbOrganization().trim().length() != 0){
				isValid = false;
				String error = "Organization is no longer valid";
				MessagePopupUtils.error(error, null);
			}
		}else{
			MessagePopupUtils.error(resultIsActiveOrganization.getFirstErrorMessage(), null);
		}
		return isValid;
	}

	
	/** upload photo */
	@Listen("onUpload=#btnUpload")
	public void onUploadClick(UploadEvent event) throws Exception{
		ReviewOutsourceEmployeeViewModel viewModel=new ReviewOutsourceEmployeeViewModel();
    	mapComponentsToViewModel(viewModel);
    	viewModel.setTxtEmployeePhoto("");
	    viewModel.setTxtFileNameWithoutExt("");
	    viewModel.setTxtExt("");
    	bindViewModelToComponents(viewModel);
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
		ReviewOutsourceEmployeeViewModel viewModel = new ReviewOutsourceEmployeeViewModel();
		mapComponentsToViewModel(viewModel);

		ListModelList<DocumentITMS> model=viewModel.getDocumentModel();
		Iterator<Listitem> listSelection = lsbAttachmentCertificates.getSelectedItems().iterator();
		List<Integer> listDeleteUI = new ArrayList<>();
		
		while(listSelection.hasNext()){
			Listitem item = listSelection.next();
			listDeleteUI.add(item.getIndex());
			lstRemoveAttachments.add(model.get(item.getIndex()).getDocumentId());
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
		ReviewOutsourceEmployeeViewModel viewModel = new ReviewOutsourceEmployeeViewModel();
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
		visibleFunction.setVisible(false);
		visibleRetaledLob.setVisible(false);
		visibleCertifications.setVisible(false);
//		visibleOutsourceCertificateFile.setVisible(false);
		visibleCompetency.setVisible(false);
		visibleRelatedContract.setVisible(false);
	}
	
	@Listen("onSelect=#lstboxSubFormResource")
	public void lstboxSubFormResourceOnChange(){

		ReviewOutsourceEmployeeViewModel viewModel=new ReviewOutsourceEmployeeViewModel();
		mapComponentsToViewModel(viewModel);
		setVisibleSubForm();
		showHidePersonalInformation.setVisible(false);
		String selected=SubFormRegsiterOutsourceEnum.getSubFormReviewResourceValue(viewModel.getLstboxSubFormResourceId());
		if(selected.equals(SubFormRegsiterOutsourceEnum.PERSONAL_INFORMATION.getName())){
			showHidePersonalInformation.setVisible(true);
		}else if(selected.equals(SubFormRegsiterOutsourceEnum.RELATED_LOB.getName())){
			visibleRetaledLob.setVisible(true);
		}else if(selected.equals(SubFormRegsiterOutsourceEnum.FUNCTION.getName())){
			visibleFunction.setVisible(true);
		}else if(selected.equals(SubFormRegsiterOutsourceEnum.CERTIFICATIONS.getName())){
			visibleCertifications.setVisible(true);
		}
		/*else if(selected.equals(SubFormRegsiterOutsourceEnum.CERTIFICATES.getName())){
			visibleOutsourceCertificateFile.setVisible(true);
		}*/
		else if(selected.equals(SubFormRegsiterOutsourceEnum.COMPETENCY.getName())){
			visibleCompetency.setVisible(true);
		}else if(selected.equals(SubFormRegsiterOutsourceEnum.RELATED_CONTRACT.getName())){
			visibleRelatedContract.setVisible(true);
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
	
	@Listen("onDownloadFile=#lsbAttachmentCertificates")
	public void downloadFile(Event event) throws FileNotFoundException {
		DocumentITMS docITMS = (DocumentITMS) event.getData();
		
		System.out.println("docITMS.getDocumentPath() "+docITMS.getDocumentPath());
		if(null != docITMS && null != docITMS.getDocumentName()){
			FileUtils.downloadFile(docITMS.getDocumentPath()+docITMS.getDocumentName(), "");
		}
	}
	
	@Listen("onClick = #btnCancel")
	public void btnCancelOnClick(Event event) {
		if (hasChanged()) {
			MessagePopupUtils.confirmUnsavedData(null, new EventListener<Event>() {
				@Override
				public void onEvent(Event arg0) throws Exception {
					try {
						Utils.detachSelectedPanel((Window) getSelf());
					} catch (Exception e) {
						e.printStackTrace();
						getSelf().detach();
					}
				}
			}, null);
		} else {
			MessagePopupUtils.confirmCancel(null, new EventListener<Event>() {
				@Override
				public void onEvent(Event arg0) throws Exception {
					try {
						Utils.detachSelectedPanel((Window) getSelf());
					} catch (Exception e) {
						e.printStackTrace();
						getSelf().detach();
					}
				}
			}, null);
		}
	}
	
	public void cancel() {
		try {
			String moduleName = "Review Outsource Employee";
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
		
		/** Dashbord / Email Link */
		if (arg.get("personId")!=null && 
				arg.get("caller") != null && 
				arg.get("caller").equals("NOTIFICATION")) {
			return FormMode.READ_ONLY;
		}else if(arg.get("personId")!=null && 
				arg.get("_caller") != null && 
				arg.get("_caller").equals("EMAIL_LINK")){
			return FormMode.READ_ONLY;
		}
		
		List<String> lstResponsibility = getRolesOrResponsibilitiesOrFieldPermissionsNames();
		boolean asOutsourceEmployee = false;
		boolean isTerminated=false;
		boolean asVendorManagement=false;
		for (String resp : lstResponsibility) {
			if (resp.equals("OUTSOURCE_EMPLOYEE")) {
				asOutsourceEmployee = true;
			}
			if(resp.equals("TERMINATED")){
				isTerminated=true;
			}
			
			if(resp.equals("VENDOR_MANAGEMENT")){
				asVendorManagement=true;
			}
		}
		/** Terminated == 1 */
		if(isTerminated==true){
			return FormMode.READ_ONLY;
		}
		
		if(asOutsourceEmployee==true){
			/***  
			 *   row effective start hiden,
			 *   Supervisor rating pada Competency di Hidden, 
			 *   row height di perbesar (kasih spasi 60px)
			 *   Sub Form Related Contract Tidak di tampilkan 
			 *   */
			chkTerminate.setVisible(false);
			lstboxRelatedContract.setVisible(false);
			lstboxCompetencyBasdedOnFunction.setDisabled(true);
			visibleSPVRating.setVisible(false);
			visibleRow.setVisible(false);
			settingRowTwo.setHeight("60px");
			settingRowThree.setHeight("60px");
			settingRowFour.setHeight("60px");
		}
		
		if(asVendorManagement==true){
			return FormMode.READ_ONLY;
		}
		
		return FormMode.EDIT;
	}

	
	public long ITManagement() {
		return Roles.IT_MANAGEMENT.getId().intValue();
	}

	public long ResManagementAdmin() {
		return Roles.RESOURCE_MANAGEMENT_ADMIN.getId().intValue();
	}

	public long SUPERVISIOR() {
		return Roles.SUPERVISIOR.getId().intValue();
	}

	public long OutsourceEmployee() {
		return Roles.OUTSOURCE_EMPLOYEE.getId().intValue();
	}

	public long RESStaf() {
		return Roles.RESOURCE_STAFF.getId().intValue();
	}
	
	private long INTERNAL_EMPLOYEE() {
		return Roles.INTERNAL_EMPLOYEE.getId().intValue();
	}
	public long vendorManagementAdmin(){
		return Roles.VENDOR_MANAGEMENT_ADMIN.getId().intValue();
	}
	
	@Override
	protected List<String> getRolesOrResponsibilitiesOrFieldPermissionsNames() {
		// SET RESPONSIBILITY PERMISION
   	   List<String> lstPermission = new ArrayList<>();
	   lstPermission.add("DEFAULT"); 
	   
	   if (Utils.getSessionUser().getSecurity() != null && Utils.getSecurity().getResponsibilityId() != null) {
			Long responsibilityId = Utils.getSessionUser().getSecurity().getResponsibilityId();
			Long executorId = Utils.getSessionUserId();
			Long outEmployeeId=Long.parseLong(arg.get("personId").toString());

			/** Terminated Data*/
			if(outEmployeeId!=null){
				ServiceResult<OutEmployee> resultDataOutEmployee = resourceService.getActiveOutEmployeeById(outEmployeeId, responsibilityId, executorId);
				if(!resultDataOutEmployee.isSuccess()){
					MessagePopupUtils.error(resultDataOutEmployee.getFirstErrorMessage(), null);
					return lstPermission;
				}
				
				OutEmployee outEmployee=resultDataOutEmployee.getResult();
				if(outEmployee.getIsTerminated()!=null && outEmployee.getIsTerminated().longValue()==1L){
					lstPermission.add("TERMINATED");
					return lstPermission;
				}
			}
			/** Responsibility */
			Long currentResponsibilityId = roleRespMapService.getRoleByResponsibilityId(responsibilityId, executorId).getResult().getId();
			if (currentResponsibilityId == ITManagement()) {
				lstPermission.add("IT_MANAGEMENT");
			} else if (currentResponsibilityId == ResManagementAdmin()) {
				lstPermission.add("RESOURCE_MANAGEMENT_ADMIN");
			} else if (currentResponsibilityId == SUPERVISIOR()) {
				lstPermission.add("SUPERVISIOR");
			} else if (currentResponsibilityId == OutsourceEmployee()) {
				lstPermission.add("OUTSOURCE_EMPLOYEE");
			} else if (currentResponsibilityId == RESStaf()) {
				lstPermission.add("RESOURCE_STAFF");
			}else if(currentResponsibilityId==INTERNAL_EMPLOYEE()){
				lstPermission.add("INTERNAL_EMPLOYEE");
			}else if(currentResponsibilityId==vendorManagementAdmin()){
				lstPermission.add("VENDOR_MANAGEMENT");
			}
		}else{
			if (null != arg && arg.get("caller") != null) {
				if(arg.get("caller").equals("NOTIFICATION")){
					lstPermission.add("DASHBORD");
					return lstPermission;
				}else if(arg.get("_caller").equals("EMAIL_LINK")){
					lstPermission.add("EMAIL");
					return lstPermission;
				}
			}
		}
		return lstPermission;
	}
	
	@Listen("onClickDetail=#lstboxCompetencyBasdedOnFunction")
	public void lstboxCompetencyBasdedOnFunctionOnClickDetail(Event event){
		openDetailFormCompetencyDetail(event);
	}
	private void openDetailFormCompetencyDetail(Event event) {

		if (event != null) {
			HashMap<String, SerializableEventListener<Event>> tabEvents = new HashMap<>();
			HashMap<String, SerializableEventListener<Event>> windowEvents = new HashMap<>();
			HashMap<String, Object> windowParams = new HashMap<>();
			String formLabel = "Competency Detail";
			
			CompetencyBasedFunction data=(CompetencyBasedFunction) event.getData();
			windowParams.put("formMode",  FormMode.READ_ONLY);
			windowParams.put("competencyId", data.getCompetencyId());
			
			tabEvents.put(Events.ON_CLOSE, new SerializableEventListener<Event>() {

				private static final long serialVersionUID = 5449856818609308110L;

				@Override
				public void onEvent(Event evt) throws Exception {
					evt.stopPropagation();
					final Tab target = (Tab) evt.getTarget();
					MessagePopupUtils.confirm(Labels.getLabel("messagebox.closeTabConfirm"), new EventListener<Event>() {
						@Override
						public void onEvent(Event arg0) throws Exception {
							target.close();
						}
					}, null);
				}
			});
			
			tabEvents.put("onRequestReload", new SerializableEventListener<Event>() {
				private static final long serialVersionUID = -4610626759626474272L;
				@Override
				public void onEvent(Event event) throws Exception {
//					doSearchData();
				}
			});

			System.out.println("Competency ID  "+data.getCompetencyId());
			ServiceResult<Tab> tabCreationResult = ComponentUtils.createTab(this.getSelf(), formLabel.replace(" ", ""), formLabel, ITMSPages.CREATE_REVIEW_COMPETENCY.getUrl(), tabEvents, windowEvents, windowParams);
			if (!tabCreationResult.isSuccess()) {
				MessagePopupUtils.errorTabAlreadyOpen(Labels.getLabel("sc.tabAlreadyOpenDetail"), null);
			}
		}
	}

	@Listen("onClickDetail=#lstboxRelatedContract")
	public void lstboxRelatedContractOnClick(Event event){
		openDetailForm(event);
	}
	
	private void openDetailForm(Event event) {

		if (event != null) {
			
			HashMap<String, SerializableEventListener<Event>> tabEvents = new HashMap<>();
			HashMap<String, SerializableEventListener<Event>> windowEvents = new HashMap<>();
			HashMap<String, Object> windowParams = new HashMap<>();
			String formLabel = "Review Contract";
			
			Contract data=(Contract) event.getData();
			windowParams.put("formMode",  FormMode.READ_ONLY);
			windowParams.put("contractId", data.getContractId());
			windowParams.put("caller", "NOTIFICATION");
			
			tabEvents.put(Events.ON_CLOSE, new SerializableEventListener<Event>() {

				private static final long serialVersionUID = 5449856818609308110L;

				@Override
				public void onEvent(Event evt) throws Exception {
					evt.stopPropagation();
					final Tab target = (Tab) evt.getTarget();
					MessagePopupUtils.confirm(Labels.getLabel("messagebox.closeTabConfirm"), new EventListener<Event>() {
						@Override
						public void onEvent(Event arg0) throws Exception {
							target.close();
						}
					}, null);
				}
			});
			
			tabEvents.put("onRequestReload", new SerializableEventListener<Event>() {
				private static final long serialVersionUID = -4610626759626474272L;
				@Override
				public void onEvent(Event event) throws Exception {
//					doSearchData();
				}
			});

			ServiceResult<Tab> tabCreationResult = ComponentUtils.createTab(this.getSelf(), formLabel.replace(" ", ""), formLabel, ITSMPages.REVIEW_CONTRACT.getUrl(), tabEvents, windowEvents, windowParams);
			if (!tabCreationResult.isSuccess()) {
				MessagePopupUtils.errorTabAlreadyOpen(Labels.getLabel("sc.tabAlreadyOpenDetail"), null);
			}
		}
	}
}