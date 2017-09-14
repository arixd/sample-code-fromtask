package id.co.fifgroup.fifgroup_rm.constanta;

import java.util.ArrayList;
import java.util.List;

import id.co.fifgroup.fifgroup_itms.global.model.IdValue;

public enum SubFormRegsiterOutsourceEnum {
	PERSONAL_INFORMATION("Personal Information",1L),
	RELATED_LOB("Related LOB",2L),
	FUNCTION("Function",3L),
	CERTIFICATIONS("Certifications",4L),
	/*CERTIFICATES("Certificates",5L),*/
	COMPETENCY("Competency",5L),
	RELATED_CONTRACT("Related Contracts",6L);
	
	private String name;
	private Long id;
	private SubFormRegsiterOutsourceEnum(String name, Long id) {
		this.name = name;
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public Long getId() {
		return id;
	}

	public static List<IdValue> getAllSubFormResource(){
		List<IdValue> subFormEnum=new ArrayList<IdValue>();
		subFormEnum.add(new IdValue(SubFormRegsiterOutsourceEnum.PERSONAL_INFORMATION.getId(), SubFormRegsiterOutsourceEnum.PERSONAL_INFORMATION.getName()));
		subFormEnum.add(new IdValue(SubFormRegsiterOutsourceEnum.RELATED_LOB.getId(), SubFormRegsiterOutsourceEnum.RELATED_LOB.getName()));
		subFormEnum.add(new IdValue(SubFormRegsiterOutsourceEnum.FUNCTION.getId(), SubFormRegsiterOutsourceEnum.FUNCTION.getName()));
		subFormEnum.add(new IdValue(SubFormRegsiterOutsourceEnum.CERTIFICATIONS.getId(), SubFormRegsiterOutsourceEnum.CERTIFICATIONS.getName()));
//		subFormEnum.add(new IdValue(SubFormRegsiterOutsourceEnum.CERTIFICATES.getId(), SubFormRegsiterOutsourceEnum.CERTIFICATES.getName()));
		return subFormEnum;
	}
	
	public static String getSubFormResourceValue(Long id){
		if(id==SubFormRegsiterOutsourceEnum.PERSONAL_INFORMATION.getId()){
			return SubFormRegsiterOutsourceEnum.PERSONAL_INFORMATION.getName();
		}else if(id==SubFormRegsiterOutsourceEnum.RELATED_LOB.getId()){
			return SubFormRegsiterOutsourceEnum.RELATED_LOB.getName();
		}else if(id==SubFormRegsiterOutsourceEnum.FUNCTION.getId()){
			return SubFormRegsiterOutsourceEnum.FUNCTION.getName();
		}else if(id==SubFormRegsiterOutsourceEnum.CERTIFICATIONS.getId()){
			return SubFormRegsiterOutsourceEnum.CERTIFICATIONS.getName();
		}
		/*else if(id==SubFormRegsiterOutsourceEnum.CERTIFICATES.getId()){
			return SubFormRegsiterOutsourceEnum.CERTIFICATES.getName();
		}*/
		return "%%";
	}
	
	public static List<IdValue> getAllSubFormReviewResource(){
		List<IdValue> subFormEnum=new ArrayList<IdValue>();
		subFormEnum.add(new IdValue(SubFormRegsiterOutsourceEnum.PERSONAL_INFORMATION.getId(), SubFormRegsiterOutsourceEnum.PERSONAL_INFORMATION.getName()));
		subFormEnum.add(new IdValue(SubFormRegsiterOutsourceEnum.RELATED_LOB.getId(), SubFormRegsiterOutsourceEnum.RELATED_LOB.getName()));
		subFormEnum.add(new IdValue(SubFormRegsiterOutsourceEnum.FUNCTION.getId(), SubFormRegsiterOutsourceEnum.FUNCTION.getName()));
		subFormEnum.add(new IdValue(SubFormRegsiterOutsourceEnum.CERTIFICATIONS.getId(), SubFormRegsiterOutsourceEnum.CERTIFICATIONS.getName()));
//		subFormEnum.add(new IdValue(SubFormRegsiterOutsourceEnum.CERTIFICATES.getId(), SubFormRegsiterOutsourceEnum.CERTIFICATES.getName()));
		subFormEnum.add(new IdValue(SubFormRegsiterOutsourceEnum.COMPETENCY.getId(), SubFormRegsiterOutsourceEnum.COMPETENCY.getName()));
		subFormEnum.add(new IdValue(SubFormRegsiterOutsourceEnum.RELATED_CONTRACT.getId(), SubFormRegsiterOutsourceEnum.RELATED_CONTRACT.getName()));
		return subFormEnum;
	}
	
	public static String getSubFormReviewResourceValue(Long id){
		if(id==SubFormRegsiterOutsourceEnum.PERSONAL_INFORMATION.getId()){
			return SubFormRegsiterOutsourceEnum.PERSONAL_INFORMATION.getName();
		}else if(id==SubFormRegsiterOutsourceEnum.RELATED_LOB.getId()){
			return SubFormRegsiterOutsourceEnum.RELATED_LOB.getName();
		}else if(id==SubFormRegsiterOutsourceEnum.FUNCTION.getId()){
			return SubFormRegsiterOutsourceEnum.FUNCTION.getName();
		}else if(id==SubFormRegsiterOutsourceEnum.CERTIFICATIONS.getId()){
			return SubFormRegsiterOutsourceEnum.CERTIFICATIONS.getName();
		}
		/*else if(id==SubFormRegsiterOutsourceEnum.CERTIFICATES.getId()){
			return SubFormRegsiterOutsourceEnum.CERTIFICATES.getName();
		}*/
		else if(id==SubFormRegsiterOutsourceEnum.COMPETENCY.getId()){
			return SubFormRegsiterOutsourceEnum.COMPETENCY.getName();
		}else if(id==SubFormRegsiterOutsourceEnum.RELATED_CONTRACT.getId()){
			return SubFormRegsiterOutsourceEnum.RELATED_CONTRACT.getName();
		}
		return "%%";
	}
	
	
	public static String getSubFormReviewResourceOutsourceValue(Long id){
		if(id==SubFormRegsiterOutsourceEnum.PERSONAL_INFORMATION.getId()){
			return SubFormRegsiterOutsourceEnum.PERSONAL_INFORMATION.getName();
		}else if(id==SubFormRegsiterOutsourceEnum.RELATED_LOB.getId()){
			return SubFormRegsiterOutsourceEnum.RELATED_LOB.getName();
		}else if(id==SubFormRegsiterOutsourceEnum.FUNCTION.getId()){
			return SubFormRegsiterOutsourceEnum.FUNCTION.getName();
		}else if(id==SubFormRegsiterOutsourceEnum.CERTIFICATIONS.getId()){
			return SubFormRegsiterOutsourceEnum.CERTIFICATIONS.getName();
		}
		/*else if(id==SubFormRegsiterOutsourceEnum.CERTIFICATES.getId()){
			return SubFormRegsiterOutsourceEnum.CERTIFICATES.getName();
		}*/
		else if(id==SubFormRegsiterOutsourceEnum.COMPETENCY.getId()){
			return SubFormRegsiterOutsourceEnum.COMPETENCY.getName();
		}
		return "%%";
	}
	
	public static List<IdValue> getAllSubFormReviewOutsource() {
		List<IdValue> subFormEnum=new ArrayList<IdValue>();
		subFormEnum.add(new IdValue(SubFormRegsiterOutsourceEnum.PERSONAL_INFORMATION.getId(), SubFormRegsiterOutsourceEnum.PERSONAL_INFORMATION.getName()));
		subFormEnum.add(new IdValue(SubFormRegsiterOutsourceEnum.RELATED_LOB.getId(), SubFormRegsiterOutsourceEnum.RELATED_LOB.getName()));
		subFormEnum.add(new IdValue(SubFormRegsiterOutsourceEnum.FUNCTION.getId(), SubFormRegsiterOutsourceEnum.FUNCTION.getName()));
		subFormEnum.add(new IdValue(SubFormRegsiterOutsourceEnum.CERTIFICATIONS.getId(), SubFormRegsiterOutsourceEnum.CERTIFICATIONS.getName()));
//		subFormEnum.add(new IdValue(SubFormRegsiterOutsourceEnum.CERTIFICATES.getId(), SubFormRegsiterOutsourceEnum.CERTIFICATES.getName()));
		subFormEnum.add(new IdValue(SubFormRegsiterOutsourceEnum.COMPETENCY.getId(), SubFormRegsiterOutsourceEnum.COMPETENCY.getName()));
		return subFormEnum;
	}
	
	public static List<IdValue> getAllSubFormReviewInternalEmployee() {
		List<IdValue> subFormEnum=new ArrayList<IdValue>();
		subFormEnum.add(new IdValue(SubFormRegsiterOutsourceEnum.COMPETENCY.getId(), SubFormRegsiterOutsourceEnum.COMPETENCY.getName()));
		subFormEnum.add(new IdValue(SubFormRegsiterOutsourceEnum.FUNCTION.getId(), SubFormRegsiterOutsourceEnum.FUNCTION.getName()));
		subFormEnum.add(new IdValue(SubFormRegsiterOutsourceEnum.RELATED_LOB.getId(), SubFormRegsiterOutsourceEnum.RELATED_LOB.getName()));
		return subFormEnum;
	}
	public static String getSubFormReviewInternalValue(Long id) {
		if(id==SubFormRegsiterOutsourceEnum.COMPETENCY.getId()){
			return SubFormRegsiterOutsourceEnum.COMPETENCY.getName();
		}else if(id==SubFormRegsiterOutsourceEnum.FUNCTION.getId()){
			return SubFormRegsiterOutsourceEnum.FUNCTION.getName();
		}else if(id==SubFormRegsiterOutsourceEnum.RELATED_LOB.getId()){
			return SubFormRegsiterOutsourceEnum.RELATED_LOB.getName();
		}
		return "%%";
	}
	/**Keterangan :
	 * adanya perubahan sub for setelah dilakukan UAT, berupa CR Yaitu :
	 * memindahkan CERTIFICATES di gabungkan dengan CERTIFICATIONS, jadi 
	 * tidak ada lagih CERTIFICATES di dalam drop down list */
	
}
