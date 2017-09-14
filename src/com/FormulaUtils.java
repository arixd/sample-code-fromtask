package id.co.fifgroup.fifgroup_itms.kpi.utils;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDateTime;
import id.co.fifgroup.fifgroup_itms.util.CustomDateUtils;
import id.co.fifgroup.fifgroup_itms.util.Utils;
import id.co.fifgroup.fifgroup_itms.kpi.model.ITSMTicketHolidayDate;
import id.co.fifgroup.fifgroup_itms.kpi.model.KPIServiceHourRest;
import id.co.fifgroup.fifgroup_itms.kpi.model.KPITicketResponTime;

public class FormulaUtils {
	
	
	 /**
    
   
    * Untuk menghitung Aging antara 2 rentang waktu
    * @param endDateParam    
    *    Hari/tgl akhir
    * @param startDateParam   
    *    Hari/tgl start
    * @param sourceData       
    *    Berisi Model KPITicketResponTime
    *    Model ini tidak hanya untuk responTime, tetapi dapat di pergunakan untuk 
    *    ResolveTime,ResolusiTime, tergantung kebutuhan.
    *    Jika membutuhkan adanya penambahan attribut, dapat di add pada baris yang paling bawah 
    *    kata "Tambahkan attribut dibawah ini, jika suatu saat membutuhkan " 
    * @param ticketRests
    *    Waktu istirahat sesuai jam kerja
    * @param ticketHolidays
    *    waktu jadwal masuk / libur kerja sesuai dengan hari yang telah di tentukan
    * @return
    *    Return adalah Long yang di anggap sebagai detik, dan dapat di olah / convert ke jam , menit , detik
    *    ex :
    *    hasil return x=1200, b
    
    * Contoh Kasus Menghitung lama waktu seseorang mengerjakan task 
    * Untuk START 
    * Jika seorang diberikan task sebelum jam masuk kerja maka geserkan waktu start task tersebut ke jam masuk kerja
    *     misal task di assigned 7:00 AM, jam masuk adalah jam 8:00 AM
    * Jika seorang diberikan task pada waktu istirahat, maka geserkan waktu start task ke jam selesai istirahat
    *     misal task di assigned 11:02 A, jam istirahat 11:00-12:00 AM -> Geserkan jam 11:02 ke jam 12:00
	  * Jika seorang diberikan task pada waktu hari libur, geserkan ke hari kapan dia masuk... 
    *     jika besoknya libur, maka geserkan lagi sampe ketemu hari masuk
    * Jika seorang diberikan task pada waktu selesai kerja, misal jam selesai kerja jam 5, task masuk jam 5:30
    *     maka geserkan ke hari besok 
    *          jika besok libur 
    *               geserkan sampai hari masuk 
    
    * Untuk END task
    * Jika seorang menyelesaikan task sebelum jam masuk kerja maka geserkan waktu end tersebut ke jam selesai bekerja yang kemarin
    *     misal task di selesai dikerjakan jam  5:10 PM, jam selesai kerja  adalah jam 5:00 PM
    *     maka geserkan waktu jam 5:10 PM -> 5:00 PM
    * Jika seorang selesai mengerjakan pas waktu istirahat, maka geserkan waktu end task ke jam mulai istirahat 
    *........... dll 
    * jika selesai di hari libut geserkan ke hari kemarinya kapan dia masuk
    
    */
	public Long calculateAging(LocalDateTime endDateParam, LocalDateTime startDateParam, KPITicketResponTime sourceData,
			List<KPIServiceHourRest> ticketRests, List<ITSMTicketHolidayDate> ticketHolidays) {
		boolean validStart = false;
		Long totalAging=new Long(0L);
		do {
			switch (startDateParam.getDayOfWeek()) {
				case DateTimeConstants.MONDAY: {
												if (sourceData.getMondayFlag().intValue() == 1) {
													// // if working
													   Integer includeHoliday=sourceData.getIncludeHoliday();
													   boolean isHoliday=isTheDateIsHolidayDate(startDateParam,endDateParam,includeHoliday,ticketHolidays);
													   if(isHoliday){
														    Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
															// will get next day with time 00:00:00
															startDateParam=LocalDateTime.fromDateFields(startDateParamTemp).plusDays(1);
													   }
													/**
													 * @ Compare Only Time in date
													 * @ start < start work
													 * */
													 if(CustomDateUtils.compareTime(new DateTime(startDateParam.toDateTime()), new DateTime(sourceData.getMondayFromTime()))<0){
					                            		    // set hour date startDateParam, to hour  :00:00:00 {datestartDateParam 00:00:00}
															 /** menghilangkan menit dan detik, jika tidak 
					                            		     maka hanya tgl saja yang berubah, jam tidak 
					                            		     contoh 01/01/2017 17:20:00 plus(1) --> 01/02/2017 17:20:00 */
															Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
															LocalDateTime localReponStart=LocalDateTime.fromDateFields(startDateParamTemp);
															// move Date with hour 00:00:00 to hour start work 
															localReponStart=localReponStart.plusHours(sourceData.getMondayFromTime().getHours());
															localReponStart=localReponStart.plusMinutes(sourceData.getMondayFromTime().getMinutes());
															localReponStart=localReponStart.plusSeconds(sourceData.getMondayFromTime().getSeconds());
															startDateParam=localReponStart;
															validStart=true;
						                             }else if(CustomDateUtils.compareTime(new DateTime(startDateParam.toDateTime()), new DateTime(sourceData.getMondayToTime()))>0){
														// add to next day 
					                            		 // if start >= work end
						                            	 // ubah jam ke 00:00:00 
						                            	 // +1 -> netx day with 00:00:00
						                            	 // move 00:00:00 ke jam mulai bekerja
						                            	    Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
															LocalDateTime localstartDateParam=LocalDateTime.fromDateFields(startDateParamTemp);
															//+1 day
															localstartDateParam=localstartDateParam.plusDays(1);
															// move Date with hour 00:00:00  start work (PINDAHKAN JAM 00:00:00 ke jam mulai bekerja)
															localstartDateParam=localstartDateParam.plusHours(sourceData.getMondayFromTime().getHours());
															localstartDateParam=localstartDateParam.plusMinutes(sourceData.getMondayFromTime().getMinutes());
															localstartDateParam=localstartDateParam.plusSeconds(sourceData.getMondayFromTime().getSeconds());
															startDateParam=localstartDateParam;
													}else{
														/**
														 * if start >=work start dan start <= work end 
														 * check rest break
														 * */
														validStart=true;
														boolean isStartOnRest=false;
														for(KPIServiceHourRest ticketRest:ticketRests){
															if(DateTimeConstants.MONDAY==ticketRest.getDay()){
																LocalDateTime restStart=LocalDateTime.fromDateFields(ticketRest.getFromTimeRest());
																LocalDateTime restEnd=LocalDateTime.fromDateFields(ticketRest.getToTimeRest());
																if(CustomDateUtils.compareTime(new DateTime(startDateParam.toDateTime()), new DateTime(restStart.toDateTime()))>=0 && CustomDateUtils.compareTime(new DateTime(startDateParam.toDateTime()), new DateTime(restEnd.toDateTime()))<0){
																	// if response start >= rest start AND response start < rest end
																	// means response start is within rest time
																	// move response start to rest end 
																	Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
																	LocalDateTime localstartDateParam=LocalDateTime.fromDateFields(startDateParamTemp);
																	
																	// jam :00:00:00 move ke jam setelah istirahat
																	localstartDateParam=localstartDateParam.plusHours(restEnd.toDate().getHours());
																	localstartDateParam=localstartDateParam.plusMinutes(restEnd.toDate().getMinutes());
																	localstartDateParam=localstartDateParam.plusSeconds(restEnd.toDate().getSeconds());
																	//startDateParam=endRest
																	startDateParam=localstartDateParam;
																	isStartOnRest=true;
																}
															}
															if(isStartOnRest)break;
														}
														
													}
													
												}else{ 
													
													/**jika tidak ada ini maka akan loop terus menerus di hari berikutnya jika hari ini libur
													 * jika tidak masuk add ke hari berikutnya
													 * SUNDAY
													 * move to next day ke hari senin */
													Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
													LocalDateTime localstartDateParam=LocalDateTime.fromDateFields(startDateParamTemp);
													//+1 day
													localstartDateParam=localstartDateParam.plusDays(1);
													startDateParam=localstartDateParam;
												}
												break;
				} // end cast monday
				case DateTimeConstants.TUESDAY: {
					                            // untuk hari selasa 
					                             if(sourceData.getTuesdayFlag().intValue()==1){
					                            	   Integer includeHoliday=sourceData.getIncludeHoliday();
													   boolean isHoliday=isTheDateIsHolidayDate(startDateParam,endDateParam,includeHoliday,ticketHolidays);
													   if(isHoliday){
														    Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
															// will get next day with time 00:00:00
															startDateParam=LocalDateTime.fromDateFields(startDateParamTemp).plusDays(1);
													   }
					                            	 //start < start work
					                            	// @@ jika dari LocalDateTime di masukan ke new DateTime, maka variabel LocalDateTime harus ke toDateTime
													// @@ jika dari Date  di masukan ke new DateTime, maka variabel tidak dirubah jadi apa2
					                            	 
					                            	 if(CustomDateUtils.compareTime(new DateTime(startDateParam.toDateTime()), new DateTime(sourceData.getTuesdayFromTime()))<0){
				                            		    // set hour date startDateParam, to hour  :00:00:00 {datestartDateParam 00:00:00}
														Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
														LocalDateTime localReponStart=LocalDateTime.fromDateFields(startDateParamTemp);
														// move Date with hour 00:00:00 to hour start work 
														localReponStart=localReponStart.plusHours(sourceData.getTuesdayFromTime().getHours());
														localReponStart=localReponStart.plusMinutes(sourceData.getTuesdayFromTime().getMinutes());
														localReponStart=localReponStart.plusSeconds(sourceData.getTuesdayFromTime().getSeconds());
														startDateParam=localReponStart;
														validStart=true;
					                            	 }else if(CustomDateUtils.compareTime(new DateTime(startDateParam.toDateTime()), new DateTime(sourceData.getTuesdayToTime()))>0){
					                            		//if start>work end, pindahkan ke besoknya
														//move to next day
														Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
														LocalDateTime localstartDateParam=LocalDateTime.fromDateFields(startDateParamTemp);
														//+1 day
														localstartDateParam=localstartDateParam.plusDays(1);
														// move Date with hour 00:00:00 to hour start work 
														localstartDateParam=localstartDateParam.plusHours(sourceData.getTuesdayFromTime().getHours());
														localstartDateParam=localstartDateParam.plusMinutes(sourceData.getTuesdayFromTime().getMinutes());
														localstartDateParam=localstartDateParam.plusSeconds(sourceData.getTuesdayFromTime().getSeconds());
														startDateParam=localstartDateParam;
					                            	 }else{
															/**
															 * if start >work start dan start < work end 
															 * check rest break
															 * */
															validStart=true;
															boolean isStartOnRest=false;
															for(KPIServiceHourRest ticketRest:ticketRests){
																if(DateTimeConstants.TUESDAY==ticketRest.getDay()){
																	LocalDateTime restStart=LocalDateTime.fromDateFields(ticketRest.getFromTimeRest());
																	LocalDateTime restEnd=LocalDateTime.fromDateFields(ticketRest.getToTimeRest());
																	if(CustomDateUtils.compareTime(new DateTime(startDateParam.toDateTime()), new DateTime(restStart.toDateTime()))>=0 && CustomDateUtils.compareTime(new DateTime(startDateParam.toDateTime()), new DateTime(restEnd.toDateTime()))<0){
																		// if response start >= rest start AND response start < rest end
																		// means response start is within rest time
																		// move response start to rest end 
																		Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
																		LocalDateTime localstartDateParam=LocalDateTime.fromDateFields(startDateParamTemp);
																		
																		// jam :00:00:00 move ke jam setelah istirahat
																		localstartDateParam=localstartDateParam.plusHours(restEnd.toDate().getHours());
																		localstartDateParam=localstartDateParam.plusMinutes(restEnd.toDate().getMinutes());
																		localstartDateParam=localstartDateParam.plusSeconds(restEnd.toDate().getSeconds());
																		//startDateParam=endRest
																		startDateParam=localstartDateParam;
																		isStartOnRest=true;
																	}
																}
																if(isStartOnRest)break;
															}
															
														}
					                            	 
					                             }else{// end flag tuesday
														/**jika tidak ada ini maka akan loop terus menerus di hari berikutnya jika hari ini libur
														 * jika tidak masuk add ke hari berikutnya
														 * SUNDAY
														 * move to next day ke hari senin */
														Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
														LocalDateTime localstartDateParam=LocalDateTime.fromDateFields(startDateParamTemp);
														//+1 day
														localstartDateParam=localstartDateParam.plusDays(1);
														startDateParam=localstartDateParam;
													}
				break;} // end cast TUESDAY
				case DateTimeConstants.WEDNESDAY: {
													if(sourceData.getWednesdayFlag().intValue()==1){
														
													   Integer includeHoliday=sourceData.getIncludeHoliday();
													   boolean isHoliday=isTheDateIsHolidayDate(startDateParam,endDateParam,includeHoliday,ticketHolidays);
													   if(isHoliday){
														    Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
															// will get next day with time 00:00:00
															startDateParam=LocalDateTime.fromDateFields(startDateParamTemp).plusDays(1);
													   }
														   
														// start< start work
														if(CustomDateUtils.compareTime(new DateTime(startDateParam.toDate().getTime()), new DateTime(sourceData.getWednesdayFromTime().getTime()))<0){
															// set hour date startDateParam, to hour  :00:00:00 {datestartDateParam 00:00:00}
															Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
															LocalDateTime localReponStart=LocalDateTime.fromDateFields(startDateParamTemp);
															// move Date with hour 00:00:00 to hour start work 
															localReponStart=localReponStart.plusHours(sourceData.getWednesdayFromTime().getHours());
															localReponStart=localReponStart.plusMinutes(sourceData.getWednesdayFromTime().getMinutes());
															localReponStart=localReponStart.plusSeconds(sourceData.getWednesdayFromTime().getSeconds());
															startDateParam=localReponStart;
															validStart=true;
														}else if(CustomDateUtils.compareTime(new DateTime(startDateParam.toDateTime()), new DateTime(sourceData.getWednesdayToTime().getTime()))>0){
															//if start>work end, pindahkan ke besoknya
															
															//move to next day
															Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
															LocalDateTime localstartDateParam=LocalDateTime.fromDateFields(startDateParamTemp);
															//+1 day
															localstartDateParam=localstartDateParam.plusDays(1);
															// move Date with hour 00:00:00 to hour start work 
															localstartDateParam=localstartDateParam.plusHours(sourceData.getWednesdayFromTime().getHours());
															localstartDateParam=localstartDateParam.plusMinutes(sourceData.getWednesdayFromTime().getMinutes());
															localstartDateParam=localstartDateParam.plusSeconds(sourceData.getWednesdayFromTime().getSeconds());
															startDateParam=localstartDateParam;
														}else{
															/**
															 * if start >work start dan start < work end 
															 * check rest break
															 * */
															validStart=true;
															boolean isStartOnRest=false;
															for(KPIServiceHourRest ticketRest:ticketRests){
																if(DateTimeConstants.WEDNESDAY==ticketRest.getDay()){
																	LocalDateTime restStart=LocalDateTime.fromDateFields(ticketRest.getFromTimeRest());
																	LocalDateTime restEnd=LocalDateTime.fromDateFields(ticketRest.getToTimeRest());
																	if(CustomDateUtils.compareTime(new DateTime(startDateParam.toDateTime()), new DateTime(restStart.toDateTime()))>=0 && CustomDateUtils.compareTime(new DateTime(startDateParam.toDateTime()), new DateTime(restEnd.toDateTime()))<0){
																		// if response start >= rest start AND response start < rest end
																		// means response start is within rest time
																		// move response start to rest end 
																		Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
																		LocalDateTime localstartDateParam=LocalDateTime.fromDateFields(startDateParamTemp);
																		
																		// jam :00:00:00 move ke jam setelah istirahat
																		localstartDateParam=localstartDateParam.plusHours(restEnd.toDate().getHours());
																		localstartDateParam=localstartDateParam.plusMinutes(restEnd.toDate().getMinutes());
																		localstartDateParam=localstartDateParam.plusSeconds(restEnd.toDate().getSeconds());
																		//startDateParam=endRest
																		startDateParam=localstartDateParam;
																		isStartOnRest=true;
																	}
																}
																if(isStartOnRest)break;
															}
															
														}
													}//end flag wednesday
													else{
														/**jika tidak ada ini maka akan loop terus menerus di hari berikutnya jika hari ini libur
														 * jika tidak masuk add ke hari berikutnya
														 * SUNDAY
														 * move to next day ke hari senin */
														Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
														LocalDateTime localstartDateParam=LocalDateTime.fromDateFields(startDateParamTemp);
														//+1 day
														localstartDateParam=localstartDateParam.plusDays(1);
														startDateParam=localstartDateParam;
													}
					// besok 
				break;} // end cast WEDNESDAY
				case DateTimeConstants.THURSDAY: {
													if(sourceData.getThursdayFlag().intValue()==1){
														   Integer includeHoliday=sourceData.getIncludeHoliday();
														   boolean isHoliday=isTheDateIsHolidayDate(startDateParam,endDateParam,includeHoliday,ticketHolidays);
														   if(isHoliday){
															    Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
																// will get next day with time 00:00:00
																startDateParam=LocalDateTime.fromDateFields(startDateParamTemp).plusDays(1);
														   }
														// start< work start
														if(CustomDateUtils.compareTime(new DateTime(startDateParam.toDate().getTime()), new DateTime(sourceData.getThursdayFromTime().getTime()))<0){
															
															// set hour date startDateParam, to hour  :00:00:00 {datestartDateParam 00:00:00}
															Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
															LocalDateTime localReponStart=LocalDateTime.fromDateFields(startDateParamTemp);
															// move Date with hour 00:00:00 to hour start work 
															localReponStart=localReponStart.plusHours(sourceData.getThursdayFromTime().getHours());
															localReponStart=localReponStart.plusMinutes(sourceData.getThursdayFromTime().getMinutes());
															localReponStart=localReponStart.plusSeconds(sourceData.getThursdayFromTime().getSeconds());
															startDateParam=localReponStart;
															validStart=true;
														}else if(CustomDateUtils.compareTime(new DateTime(startDateParam.toDate().getTime()), new DateTime(sourceData.getThursdayToTime().getTime()))>0){
															//if start>work end, pindahkan ke besoknya
															
															//move to next day
															Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
															LocalDateTime localstartDateParam=LocalDateTime.fromDateFields(startDateParamTemp);
															//+1 day
															localstartDateParam=localstartDateParam.plusDays(1);
															// move Date with hour 00:00:00 to hour start work 
															localstartDateParam=localstartDateParam.plusHours(sourceData.getThursdayFromTime().getHours());
															localstartDateParam=localstartDateParam.plusMinutes(sourceData.getThursdayFromTime().getMinutes());
															localstartDateParam=localstartDateParam.plusSeconds(sourceData.getThursdayFromTime().getSeconds());
															startDateParam=localstartDateParam;
														}else{
															/**
															 * if start >work start dan start < work end 
															 * check rest break
															 * */
															validStart=true;
															boolean isStartOnRest=false;
															for(KPIServiceHourRest ticketRest:ticketRests){
																if(DateTimeConstants.THURSDAY==ticketRest.getDay()){
																	LocalDateTime restStart=LocalDateTime.fromDateFields(ticketRest.getFromTimeRest());
																	LocalDateTime restEnd=LocalDateTime.fromDateFields(ticketRest.getToTimeRest());
																	if(CustomDateUtils.compareTime(new DateTime(startDateParam.toDateTime()), new DateTime(restStart.toDateTime()))>=0 && CustomDateUtils.compareTime(new DateTime(startDateParam.toDateTime()), new DateTime(restEnd.toDateTime()))<0){
																		// if response start >= rest start AND response start < rest end
																		// means response start is within rest time
																		// move response start to rest end 
																		Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
																		LocalDateTime localstartDateParam=LocalDateTime.fromDateFields(startDateParamTemp);
																		
																		// jam :00:00:00 move ke jam setelah istirahat
																		localstartDateParam=localstartDateParam.plusHours(restEnd.toDate().getHours());
																		localstartDateParam=localstartDateParam.plusMinutes(restEnd.toDate().getMinutes());
																		localstartDateParam=localstartDateParam.plusSeconds(restEnd.toDate().getSeconds());
																		//startDateParam=endRest
																		startDateParam=localstartDateParam;
																		isStartOnRest=true;
																	}
																}
																if(isStartOnRest)break;
															}
															
														}
														
													}else{
														/**jika tidak ada ini maka akan loop terus menerus di hari berikutnya jika hari ini libur
														 * jika tidak masuk add ke hari berikutnya
														 * SUNDAY
														 * move to next day ke hari senin */
														Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
														LocalDateTime localstartDateParam=LocalDateTime.fromDateFields(startDateParamTemp);
														//+1 day
														localstartDateParam=localstartDateParam.plusDays(1);
														startDateParam=localstartDateParam;
													}
			    break;} // end cast THURSDAY
				case DateTimeConstants.FRIDAY: {
					
													if(sourceData.getFridayFlag().intValue()==1){
														   Integer includeHoliday=sourceData.getIncludeHoliday();
														   boolean isHoliday=isTheDateIsHolidayDate(startDateParam,endDateParam,includeHoliday,ticketHolidays);
														   if(isHoliday){
															    Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
																// will get next day with time 00:00:00
																startDateParam=LocalDateTime.fromDateFields(startDateParamTemp).plusDays(1);
														   }
														
														// start< work start
														if(CustomDateUtils.compareTime(new DateTime(startDateParam.toDate().getTime()), new DateTime(sourceData.getFridayFromTime().getTime()))<0){
															
															// set hour date startDateParam, to hour  :00:00:00 {datestartDateParam 00:00:00}
															Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
															LocalDateTime localReponStart=LocalDateTime.fromDateFields(startDateParamTemp);
															// move Date with hour 00:00:00 to hour start work 
															localReponStart=localReponStart.plusHours(sourceData.getFridayFromTime().getHours());
															localReponStart=localReponStart.plusMinutes(sourceData.getFridayFromTime().getMinutes());
															localReponStart=localReponStart.plusSeconds(sourceData.getFridayFromTime().getSeconds());
															startDateParam=localReponStart;
															validStart=true;
														}else if(CustomDateUtils.compareTime(new DateTime(startDateParam.toDate().getTime()), new DateTime(sourceData.getFridayToTime().getTime()))>0){
															//if start>work end, pindahkan ke besoknya
															
															//move to next day
															Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
															LocalDateTime localstartDateParam=LocalDateTime.fromDateFields(startDateParamTemp);
															//+1 day
															localstartDateParam=localstartDateParam.plusDays(1);
															// move Date with hour 00:00:00 to hour start work 
															localstartDateParam=localstartDateParam.plusHours(sourceData.getFridayFromTime().getHours());
															localstartDateParam=localstartDateParam.plusMinutes(sourceData.getFridayFromTime().getMinutes());
															localstartDateParam=localstartDateParam.plusSeconds(sourceData.getFridayFromTime().getSeconds());
															startDateParam=localstartDateParam;
														}else{
															/**
															 * if start >work start dan start < work end 
															 * check rest break
															 * */
															validStart=true;
															boolean isStartOnRest=false;
															for(KPIServiceHourRest ticketRest:ticketRests){
																if(DateTimeConstants.FRIDAY==ticketRest.getDay()){
																	LocalDateTime restStart=LocalDateTime.fromDateFields(ticketRest.getFromTimeRest());
																	LocalDateTime restEnd=LocalDateTime.fromDateFields(ticketRest.getToTimeRest());
																	if(CustomDateUtils.compareTime(new DateTime(startDateParam.toDateTime()), new DateTime(restStart.toDateTime()))>=0 && CustomDateUtils.compareTime(new DateTime(startDateParam.toDateTime()), new DateTime(restEnd.toDateTime()))<0){
																		// if response start >= rest start AND response start < rest end
																		// means response start is within rest time
																		// move response start to rest end 
																		Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
																		LocalDateTime localstartDateParam=LocalDateTime.fromDateFields(startDateParamTemp);
																		
																		// jam :00:00:00 move ke jam setelah istirahat
																		localstartDateParam=localstartDateParam.plusHours(restEnd.toDate().getHours());
																		localstartDateParam=localstartDateParam.plusMinutes(restEnd.toDate().getMinutes());
																		localstartDateParam=localstartDateParam.plusSeconds(restEnd.toDate().getSeconds());
																		//startDateParam=endRest
																		startDateParam=localstartDateParam;
																		isStartOnRest=true;
																	}
																}
																if(isStartOnRest)break;
															}
															
														}
														
														
													}// end if flag
													else{
														/**jika tidak ada ini maka akan loop terus menerus di hari berikutnya
														 * jika tidak masuk add ke hari berikutnya
														 * SUNDAY
														 * move to next day ke hari senin */
														Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
														LocalDateTime localstartDateParam=LocalDateTime.fromDateFields(startDateParamTemp);
														//+1 day
														localstartDateParam=localstartDateParam.plusDays(1);
														startDateParam=localstartDateParam;
													}
					
				break;} // end cast FRIDAY
				case DateTimeConstants.SATURDAY: {
													if(sourceData.getSaturdayFlag().intValue()==1){

														   Integer includeHoliday=sourceData.getIncludeHoliday();
														   boolean isHoliday=isTheDateIsHolidayDate(startDateParam,endDateParam,includeHoliday,ticketHolidays);
														   if(isHoliday){
															    Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
																// will get next day with time 00:00:00
																startDateParam=LocalDateTime.fromDateFields(startDateParamTemp).plusDays(1);
														   }
															
															// start< work start
															if(CustomDateUtils.compareTime(new DateTime(startDateParam.toDate().getTime()), new DateTime(sourceData.getSaturdayFromTime().getTime()))<0){
																
																// set hour date startDateParam, to hour  :00:00:00 {datestartDateParam 00:00:00}
																Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
																LocalDateTime localReponStart=LocalDateTime.fromDateFields(startDateParamTemp);
																// move Date with hour 00:00:00 to hour start work 
																localReponStart=localReponStart.plusHours(sourceData.getSaturdayFromTime().getHours());
																localReponStart=localReponStart.plusMinutes(sourceData.getSaturdayFromTime().getMinutes());
																localReponStart=localReponStart.plusSeconds(sourceData.getSaturdayFromTime().getSeconds());
																startDateParam=localReponStart;
																validStart=true;
															}else if(CustomDateUtils.compareTime(new DateTime(startDateParam.toDate().getTime()), new DateTime(sourceData.getSaturdayToTime()))>0){
																//if start>work end, pindahkan ke besoknya
																
																//move to next day
																Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
																LocalDateTime localstartDateParam=LocalDateTime.fromDateFields(startDateParamTemp);
																//+1 day
																localstartDateParam=localstartDateParam.plusDays(1);
																// move Date with hour 00:00:00 to hour start work 
																localstartDateParam=localstartDateParam.plusHours(sourceData.getSaturdayToTime().getHours());
																localstartDateParam=localstartDateParam.plusMinutes(sourceData.getSaturdayToTime().getMinutes());
																localstartDateParam=localstartDateParam.plusSeconds(sourceData.getSaturdayToTime().getSeconds());
																startDateParam=localstartDateParam;
															}else{
																/**
																 * if start >work start dan start < work end 
																 * check rest break
																 * */
																validStart=true;
																boolean isStartOnRest=false;
																for(KPIServiceHourRest ticketRest:ticketRests){
																	if(DateTimeConstants.SATURDAY==ticketRest.getDay()){
																		LocalDateTime restStart=LocalDateTime.fromDateFields(ticketRest.getFromTimeRest());
																		LocalDateTime restEnd=LocalDateTime.fromDateFields(ticketRest.getToTimeRest());
																		if(CustomDateUtils.compareTime(new DateTime(startDateParam.toDateTime()), new DateTime(restStart.toDateTime()))>=0 && CustomDateUtils.compareTime(new DateTime(startDateParam.toDateTime()), new DateTime(restEnd.toDateTime()))<0){
																			// if response start >= rest start AND response start < rest end
																			// means response start is within rest time
																			// move response start to rest end 
																			Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
																			LocalDateTime localstartDateParam=LocalDateTime.fromDateFields(startDateParamTemp);
																			
																			// jam :00:00:00 move ke jam setelah istirahat
																			localstartDateParam=localstartDateParam.plusHours(restEnd.toDate().getHours());
																			localstartDateParam=localstartDateParam.plusMinutes(restEnd.toDate().getMinutes());
																			localstartDateParam=localstartDateParam.plusSeconds(restEnd.toDate().getSeconds());
																			//startDateParam=endRest
																			startDateParam=localstartDateParam;
																			isStartOnRest=true;
																		}
																	}
																	if(isStartOnRest)break;
																}
																
															}
															
													
													}else{
														// jika tidak masuk add ke hari berikutnya
														// SUNDAY
														//move to next day
														Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
														LocalDateTime localstartDateParam=LocalDateTime.fromDateFields(startDateParamTemp);
														//+1 day
														localstartDateParam=localstartDateParam.plusDays(1);
														startDateParam=localstartDateParam;
													}
					
				break;} // end cast SATURDAY
				case DateTimeConstants.SUNDAY: {
					                             if(sourceData.getSundayFlag().intValue()==1){

					                            	   Integer includeHoliday=sourceData.getIncludeHoliday();
													   boolean isHoliday=isTheDateIsHolidayDate(startDateParam,endDateParam,includeHoliday,ticketHolidays);
													   if(isHoliday){
														    Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
															// will get next day with time 00:00:00
															startDateParam=LocalDateTime.fromDateFields(startDateParamTemp).plusDays(1);
													   }
				                            	// start< work start
													if(CustomDateUtils.compareTime(new DateTime(startDateParam.toDate().getTime()), new DateTime(sourceData.getSaturdayFromTime().getTime()))<0){
														
														// set hour date startDateParam, to hour  :00:00:00 {datestartDateParam 00:00:00}
														Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
														LocalDateTime localReponStart=LocalDateTime.fromDateFields(startDateParamTemp);
														// move Date with hour 00:00:00 to hour start work 
														localReponStart=localReponStart.plusHours(sourceData.getSaturdayFromTime().getHours());
														localReponStart=localReponStart.plusMinutes(sourceData.getSaturdayFromTime().getMinutes());
														localReponStart=localReponStart.plusSeconds(sourceData.getSaturdayFromTime().getSeconds());
														startDateParam=localReponStart;
														validStart=true;
													}else if(CustomDateUtils.compareTime(new DateTime(startDateParam.toDate().getTime()), new DateTime(sourceData.getSaturdayToTime()))>0){
														//if start>work end, pindahkan ke besoknya
														
														//move to next day
														Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
														LocalDateTime localstartDateParam=LocalDateTime.fromDateFields(startDateParamTemp);
														//+1 day
														localstartDateParam=localstartDateParam.plusDays(1);
														// move Date with hour 00:00:00 to hour start work 
														localstartDateParam=localstartDateParam.plusHours(sourceData.getSaturdayToTime().getHours());
														localstartDateParam=localstartDateParam.plusMinutes(sourceData.getSaturdayToTime().getMinutes());
														localstartDateParam=localstartDateParam.plusSeconds(sourceData.getSaturdayToTime().getSeconds());
														startDateParam=localstartDateParam;
													}else{
														/**
														 * if start >work start dan start < work end 
														 * check rest break
														 * */
														validStart=true;
														boolean isStartOnRest=false;
														for(KPIServiceHourRest ticketRest:ticketRests){
															if(DateTimeConstants.SUNDAY==ticketRest.getDay()){
																LocalDateTime restStart=LocalDateTime.fromDateFields(ticketRest.getFromTimeRest());
																LocalDateTime restEnd=LocalDateTime.fromDateFields(ticketRest.getToTimeRest());
																if(CustomDateUtils.compareTime(new DateTime(startDateParam.toDateTime()), new DateTime(restStart.toDateTime()))>=0 && CustomDateUtils.compareTime(new DateTime(startDateParam.toDateTime()), new DateTime(restEnd.toDateTime()))<0){
																	// if response start >= rest start AND response start < rest end
																	// means response start is within rest time
																	// move response start to rest end 
																	Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
																	LocalDateTime localstartDateParam=LocalDateTime.fromDateFields(startDateParamTemp);
																	
																	// jam :00:00:00 move ke jam setelah istirahat
																	localstartDateParam=localstartDateParam.plusHours(restEnd.toDate().getHours());
																	localstartDateParam=localstartDateParam.plusMinutes(restEnd.toDate().getMinutes());
																	localstartDateParam=localstartDateParam.plusSeconds(restEnd.toDate().getSeconds());
																	//startDateParam=endRest
																	startDateParam=localstartDateParam;
																	isStartOnRest=true;
																}
															}
															if(isStartOnRest)break;
														}
														
													}// end else
					                            	 
					                             }// end if flag
					                             else{
														// jika tidak masuk add ke hari berikutnya
														// SUNDAY
														//move to next day ke hari senin
														Date startDateParamTemp=Utils.removeTime(startDateParam.toDate());
														LocalDateTime localstartDateParam=LocalDateTime.fromDateFields(startDateParamTemp);
														//+1 day
														localstartDateParam=localstartDateParam.plusDays(1);
														startDateParam=localstartDateParam;
													}
				break;} // end cast SUNDAY
			}// END SWITCH
			
			if(startDateParam.compareTo(endDateParam) > 0){
				// current response start > response end
				// then out of loop, no need further check (aging should be 0)
				break;
			}
		} while (!validStart);
		/**
		 * 
		 * Memindahkan End ke tempat yang benar
		 * Jika end di jam istirahat, maka pindahkan end ke sebelum istirahat
		 * Jika end setelah istirahat, maka 
		 * untuk perhitungan hari itu di awal mulai bekerja hitung waktu detiknya sampai istirahat
		 * 
		 * */
		
		System.out.println(" Start Respon  : "+startDateParam);
		boolean validEnd=false;
		do{
			switch (endDateParam.getDayOfWeek()) {
			case DateTimeConstants.MONDAY: {     
											//respon end < start work
											if(CustomDateUtils.compareTime(new DateTime(endDateParam.toDate().getTime()), new DateTime(sourceData.getMondayFromTime().getTime()))<0){
												// set hour date startDateParam, to hour  :00:00:00 {datestartDateParam 00:00:00}
												Date endDateParamTemp=Utils.removeTime(endDateParam.toDate());
												LocalDateTime localReponEnd=LocalDateTime.fromDateFields(endDateParamTemp);
												
												// move Date with hour 00:00:00 to hour start work 
												localReponEnd=localReponEnd.plusHours(sourceData.getMondayFromTime().getHours());
												localReponEnd=localReponEnd.plusMinutes(sourceData.getMondayFromTime().getMinutes());
												localReponEnd=localReponEnd.plusSeconds(sourceData.getMondayFromTime().getSeconds());
												endDateParam=localReponEnd;
												validEnd=true;
											}else if(CustomDateUtils.compareTime(new DateTime(endDateParam.toDate().getTime()), new DateTime(sourceData.getMondayToTime().getTime()))>0){
												//respon end > end work	
												// set hour date startDateParam, to hour  :00:00:00 {datestartDateParam 00:00:00}
												Date endDateParamTemp=Utils.removeTime(endDateParam.toDate());
												LocalDateTime localReponEnd=LocalDateTime.fromDateFields(endDateParamTemp);
												// move Date with hour 00:00:00 to hour start work 
												localReponEnd=localReponEnd.plusHours(sourceData.getMondayToTime().getHours());
												localReponEnd=localReponEnd.plusMinutes(sourceData.getMondayToTime().getMinutes());
												localReponEnd=localReponEnd.plusSeconds(sourceData.getMondayToTime().getSeconds());
												endDateParam=localReponEnd;
												validEnd=true;
											}else{
												// CHECK RESPON END IN REST
												/*** Lojik **
												 *   respondEnd=11:30
												 *   restStart=11:30 AM, restEnd=1:00:00 PM
													if(endDateParam>restStart && endDateParam<restEnd){
														1. 
														  11:30> ? N, Y
													}*/
												for(KPIServiceHourRest ticketRest:ticketRests){
													if(DateTimeConstants.MONDAY==ticketRest.getDay()){
														LocalDateTime restStart=LocalDateTime.fromDateFields(ticketRest.getFromTimeRest());
														LocalDateTime restEnd=LocalDateTime.fromDateFields(ticketRest.getToTimeRest());
														if(CustomDateUtils.compareTime(new DateTime(endDateParam.toDateTime()), new DateTime(restStart.toDateTime()))>=0 
																&& CustomDateUtils.compareTime(new DateTime(endDateParam.toDateTime()), new DateTime(restEnd.toDateTime()))<=0){
															Date endDateParamTemp=Utils.removeTime(endDateParam.toDate());
															LocalDateTime localendDateParam=LocalDateTime.fromDateFields(endDateParamTemp);
															
															// jam :00:00:00 move ke jam mulai istirahat
															localendDateParam=localendDateParam.plusHours(restStart.toDate().getHours());
															localendDateParam=localendDateParam.plusMinutes(restStart.toDate().getMinutes());
															localendDateParam=localendDateParam.plusSeconds(restStart.toDate().getSeconds());
															
															endDateParam=localendDateParam;
														}
													}
												}
												validEnd=true;
											}
											break;}
			case DateTimeConstants.TUESDAY: {
												if(CustomDateUtils.compareTime(new DateTime(endDateParam.toDate().getTime()), new DateTime(sourceData.getTuesdayFromTime().getTime()))<0){
													// set hour date startDateParam, to hour  :00:00:00 {datestartDateParam 00:00:00}
													Date endDateParamTemp=Utils.removeTime(endDateParam.toDate());
													LocalDateTime localReponEnd=LocalDateTime.fromDateFields(endDateParamTemp);
													
													// move Date with hour 00:00:00 to hour start work 
													localReponEnd=localReponEnd.plusHours(sourceData.getTuesdayFromTime().getHours());
													localReponEnd=localReponEnd.plusMinutes(sourceData.getTuesdayFromTime().getMinutes());
													localReponEnd=localReponEnd.plusSeconds(sourceData.getTuesdayFromTime().getSeconds());
													endDateParam=localReponEnd;
													validEnd=true;
												}else if(CustomDateUtils.compareTime(new DateTime(endDateParam.toDate().getTime()), new DateTime(sourceData.getTuesdayToTime().getTime()))>0){
													//respon end > end work	
													// set hour date startDateParam, to hour  :00:00:00 {datestartDateParam 00:00:00}
													Date endDateParamTemp=Utils.removeTime(endDateParam.toDate());
													LocalDateTime localReponEnd=LocalDateTime.fromDateFields(endDateParamTemp);
													// move Date with hour 00:00:00 to hour start work 
													localReponEnd=localReponEnd.plusHours(sourceData.getTuesdayToTime().getHours());
													localReponEnd=localReponEnd.plusMinutes(sourceData.getTuesdayToTime().getMinutes());
													localReponEnd=localReponEnd.plusSeconds(sourceData.getTuesdayToTime().getSeconds());
													endDateParam=localReponEnd;
													validEnd=true;
												}else{
													// CHECK RESPON END IN REST
													for(KPIServiceHourRest ticketRest:ticketRests){
														if(DateTimeConstants.TUESDAY==ticketRest.getDay()){
															LocalDateTime restStart=LocalDateTime.fromDateFields(ticketRest.getFromTimeRest());
															LocalDateTime restEnd=LocalDateTime.fromDateFields(ticketRest.getToTimeRest());
															if(CustomDateUtils.compareTime(new DateTime(endDateParam.toDateTime()), new DateTime(restStart.toDateTime()))>=0 
																	&& CustomDateUtils.compareTime(new DateTime(endDateParam.toDateTime()), new DateTime(restEnd.toDateTime()))<=0){
																Date endDateParamTemp=Utils.removeTime(endDateParam.toDate());
																LocalDateTime localendDateParam=LocalDateTime.fromDateFields(endDateParamTemp);
																
																// jam :00:00:00 move ke jam mulai istirahat
																localendDateParam=localendDateParam.plusHours(restStart.toDate().getHours());
																localendDateParam=localendDateParam.plusMinutes(restStart.toDate().getMinutes());
																localendDateParam=localendDateParam.plusSeconds(restStart.toDate().getSeconds());
																
																endDateParam=localendDateParam;
															}
														}
													}
													validEnd=true;
												}
												break;}
			case DateTimeConstants.WEDNESDAY: {
				                                // END RESPON < END WORK
												
												if(CustomDateUtils.compareTime(new DateTime(endDateParam.toDate().getTime()), new DateTime(sourceData.getWednesdayFromTime().getTime()))<0){
													// set hour date startDateParam, to hour  :00:00:00 {datestartDateParam 00:00:00}
													Date endDateParamTemp=Utils.removeTime(endDateParam.toDate());
													LocalDateTime localReponEnd=LocalDateTime.fromDateFields(endDateParamTemp);
													
													// move Date with hour 00:00:00 to hour start work 
													localReponEnd=localReponEnd.plusHours(sourceData.getWednesdayFromTime().getHours());
													localReponEnd=localReponEnd.plusMinutes(sourceData.getWednesdayFromTime().getMinutes());
													localReponEnd=localReponEnd.plusSeconds(sourceData.getWednesdayFromTime().getSeconds());
													endDateParam=localReponEnd;
													validEnd=true;
												}else if(CustomDateUtils.compareTime(new DateTime(endDateParam.toDate().getTime()), new DateTime(sourceData.getWednesdayToTime().getTime()))>0){
													//respon end > end work	
													// set hour date startDateParam, to hour  :00:00:00 {datestartDateParam 00:00:00}
													Date endDateParamTemp=Utils.removeTime(endDateParam.toDate());
													LocalDateTime localReponEnd=LocalDateTime.fromDateFields(endDateParamTemp);
													// move Date with hour 00:00:00 to hour start work 
													localReponEnd=localReponEnd.plusHours(sourceData.getWednesdayToTime().getHours());
													localReponEnd=localReponEnd.plusMinutes(sourceData.getWednesdayToTime().getMinutes());
													localReponEnd=localReponEnd.plusSeconds(sourceData.getWednesdayToTime().getSeconds());
													endDateParam=localReponEnd;
													validEnd=true;
												}else{
													// CHECK RESPON END IN REST
													for(KPIServiceHourRest ticketRest:ticketRests){
														if(DateTimeConstants.WEDNESDAY==ticketRest.getDay()){
															LocalDateTime restStart=LocalDateTime.fromDateFields(ticketRest.getFromTimeRest());
															LocalDateTime restEnd=LocalDateTime.fromDateFields(ticketRest.getToTimeRest());
															if(CustomDateUtils.compareTime(new DateTime(endDateParam.toDateTime()), new DateTime(restStart.toDateTime()))>=0 
																	&& CustomDateUtils.compareTime(new DateTime(endDateParam.toDateTime()), new DateTime(restEnd.toDateTime()))<=0){
																Date endDateParamTemp=Utils.removeTime(endDateParam.toDate());
																LocalDateTime localendDateParam=LocalDateTime.fromDateFields(endDateParamTemp);
																
																// RESPON EDN DI MOVE KE JAM MULAI ISTIRAHAT 
																// jam :00:00:00 move ke jam mulai istirahat
																localendDateParam=localendDateParam.plusHours(restStart.toDate().getHours());
																localendDateParam=localendDateParam.plusMinutes(restStart.toDate().getMinutes());
																localendDateParam=localendDateParam.plusSeconds(restStart.toDate().getSeconds());
																
																endDateParam=localendDateParam;
															}
														}
													}
													validEnd=true;
												}
				break;}
			case DateTimeConstants.THURSDAY: {
				 // END RESPON < END WORK
				
											if(CustomDateUtils.compareTime(new DateTime(endDateParam.toDate().getTime()), new DateTime(sourceData.getThursdayFromTime().getTime()))<0){
												// set hour date startDateParam, to hour  :00:00:00 {datestartDateParam 00:00:00}
												Date endDateParamTemp=Utils.removeTime(endDateParam.toDate());
												LocalDateTime localReponEnd=LocalDateTime.fromDateFields(endDateParamTemp);
												
												// move Date with hour 00:00:00 to hour start work 
												localReponEnd=localReponEnd.plusHours(sourceData.getThursdayFromTime().getHours());
												localReponEnd=localReponEnd.plusMinutes(sourceData.getThursdayFromTime().getMinutes());
												localReponEnd=localReponEnd.plusSeconds(sourceData.getThursdayFromTime().getSeconds());
												endDateParam=localReponEnd;
												validEnd=true;
											}else if(CustomDateUtils.compareTime(new DateTime(endDateParam.toDate().getTime()), new DateTime(sourceData.getThursdayToTime().getTime()))>0){
												//respon end > end work	
												// set hour date startDateParam, to hour  :00:00:00 {datestartDateParam 00:00:00}
												Date endDateParamTemp=Utils.removeTime(endDateParam.toDate());
												LocalDateTime localReponEnd=LocalDateTime.fromDateFields(endDateParamTemp);
												// move Date with hour 00:00:00 to hour start work 
												localReponEnd=localReponEnd.plusHours(sourceData.getThursdayToTime().getHours());
												localReponEnd=localReponEnd.plusMinutes(sourceData.getThursdayToTime().getMinutes());
												localReponEnd=localReponEnd.plusSeconds(sourceData.getThursdayToTime().getSeconds());
												endDateParam=localReponEnd;
												validEnd=true;
											}else{
												// CHECK RESPON END IN REST
												for(KPIServiceHourRest ticketRest:ticketRests){
													if(DateTimeConstants.THURSDAY==ticketRest.getDay()){
														LocalDateTime restStart=LocalDateTime.fromDateFields(ticketRest.getFromTimeRest());
														LocalDateTime restEnd=LocalDateTime.fromDateFields(ticketRest.getToTimeRest());
														if(CustomDateUtils.compareTime(new DateTime(endDateParam.toDateTime()), new DateTime(restStart.toDateTime()))>=0 
																&& CustomDateUtils.compareTime(new DateTime(endDateParam.toDateTime()), new DateTime(restEnd.toDateTime()))<=0){
															Date endDateParamTemp=Utils.removeTime(endDateParam.toDate());
															LocalDateTime localendDateParam=LocalDateTime.fromDateFields(endDateParamTemp);
															
															// RESPON EDN DI MOVE KE JAM MULAI ISTIRAHAT 
															// jam :00:00:00 move ke jam mulai istirahat
															localendDateParam=localendDateParam.plusHours(restStart.toDate().getHours());
															localendDateParam=localendDateParam.plusMinutes(restStart.toDate().getMinutes());
															localendDateParam=localendDateParam.plusSeconds(restStart.toDate().getSeconds());
															
															endDateParam=localendDateParam;
														}
													}
												}
												validEnd=true;
											}
												break;}
			case DateTimeConstants.FRIDAY: {
											
				// END RESPON < END WORK
											
											if(CustomDateUtils.compareTime(new DateTime(endDateParam.toDate().getTime()), new DateTime(sourceData.getFridayFromTime().getTime()))<0){
												// set hour date startDateParam, to hour  :00:00:00 {datestartDateParam 00:00:00}
												Date endDateParamTemp=Utils.removeTime(endDateParam.toDate());
												LocalDateTime localReponEnd=LocalDateTime.fromDateFields(endDateParamTemp);
												
												// move Date with hour 00:00:00 to hour start work 
												localReponEnd=localReponEnd.plusHours(sourceData.getFridayFromTime().getHours());
												localReponEnd=localReponEnd.plusMinutes(sourceData.getFridayFromTime().getMinutes());
												localReponEnd=localReponEnd.plusSeconds(sourceData.getFridayFromTime().getSeconds());
												endDateParam=localReponEnd;
												validEnd=true;
											}else if(CustomDateUtils.compareTime(new DateTime(endDateParam.toDate().getTime()), new DateTime(sourceData.getFridayToTime().getTime()))>0){
												//respon end > end work	
												// set hour date startDateParam, to hour  :00:00:00 {datestartDateParam 00:00:00}
												Date endDateParamTemp=Utils.removeTime(endDateParam.toDate());
												LocalDateTime localReponEnd=LocalDateTime.fromDateFields(endDateParamTemp);
												// move Date with hour 00:00:00 to hour start work 
												localReponEnd=localReponEnd.plusHours(sourceData.getFridayToTime().getHours());
												localReponEnd=localReponEnd.plusMinutes(sourceData.getFridayToTime().getMinutes());
												localReponEnd=localReponEnd.plusSeconds(sourceData.getFridayToTime().getSeconds());
												endDateParam=localReponEnd;
												validEnd=true;
											}else{
												// CHECK RESPON END IN REST
												for(KPIServiceHourRest ticketRest:ticketRests){
													if(DateTimeConstants.FRIDAY==ticketRest.getDay()){
														LocalDateTime restStart=LocalDateTime.fromDateFields(ticketRest.getFromTimeRest());
														LocalDateTime restEnd=LocalDateTime.fromDateFields(ticketRest.getToTimeRest());
														if(CustomDateUtils.compareTime(new DateTime(endDateParam.toDateTime()), new DateTime(restStart.toDateTime()))>=0 
																&& CustomDateUtils.compareTime(new DateTime(endDateParam.toDateTime()), new DateTime(restEnd.toDateTime()))<=0){
															Date endDateParamTemp=Utils.removeTime(endDateParam.toDate());
															LocalDateTime localendDateParam=LocalDateTime.fromDateFields(endDateParamTemp);
															
															// RESPON EDN DI MOVE KE JAM MULAI ISTIRAHAT 
															// jam :00:00:00 move ke jam mulai istirahat
															localendDateParam=localendDateParam.plusHours(restStart.toDate().getHours());
															localendDateParam=localendDateParam.plusMinutes(restStart.toDate().getMinutes());
															localendDateParam=localendDateParam.plusSeconds(restStart.toDate().getSeconds());
															
															endDateParam=localendDateParam;
														}
													}
												}
												validEnd=true;
											}
									
											break;}
			case DateTimeConstants.SATURDAY: {
												// END RESPON < END WORK
												
												if(CustomDateUtils.compareTime(new DateTime(endDateParam.toDate().getTime()), new DateTime(sourceData.getSaturdayFromTime().getTime()))<0){
													// set hour date startDateParam, to hour  :00:00:00 {datestartDateParam 00:00:00}
													Date endDateParamTemp=Utils.removeTime(endDateParam.toDate());
													LocalDateTime localReponEnd=LocalDateTime.fromDateFields(endDateParamTemp);
													
													// move Date with hour 00:00:00 to hour start work 
													localReponEnd=localReponEnd.plusHours(sourceData.getSaturdayFromTime().getHours());
													localReponEnd=localReponEnd.plusMinutes(sourceData.getSaturdayFromTime().getMinutes());
													localReponEnd=localReponEnd.plusSeconds(sourceData.getSaturdayFromTime().getSeconds());
													endDateParam=localReponEnd;
													validEnd=true;
												}else if(CustomDateUtils.compareTime(new DateTime(endDateParam.toDate().getTime()), new DateTime(sourceData.getSaturdayToTime().getTime()))>0){
													//respon end > end work	
													// set hour date startDateParam, to hour  :00:00:00 {datestartDateParam 00:00:00}
													Date endDateParamTemp=Utils.removeTime(endDateParam.toDate());
													LocalDateTime localReponEnd=LocalDateTime.fromDateFields(endDateParamTemp);
													// move Date with hour 00:00:00 to hour start work 
													localReponEnd=localReponEnd.plusHours(sourceData.getSaturdayToTime().getHours());
													localReponEnd=localReponEnd.plusMinutes(sourceData.getSaturdayToTime().getMinutes());
													localReponEnd=localReponEnd.plusSeconds(sourceData.getSaturdayToTime().getSeconds());
													endDateParam=localReponEnd;
													validEnd=true;
												}else{
													// CHECK RESPON END IN REST
													for(KPIServiceHourRest ticketRest:ticketRests){
														if(DateTimeConstants.SATURDAY==ticketRest.getDay()){
															LocalDateTime restStart=LocalDateTime.fromDateFields(ticketRest.getFromTimeRest());
															LocalDateTime restEnd=LocalDateTime.fromDateFields(ticketRest.getToTimeRest());
															if(CustomDateUtils.compareTime(new DateTime(endDateParam.toDateTime()), new DateTime(restStart.toDateTime()))>=0 
																	&& CustomDateUtils.compareTime(new DateTime(endDateParam.toDateTime()), new DateTime(restEnd.toDateTime()))<=0){
																Date endDateParamTemp=Utils.removeTime(endDateParam.toDate());
																LocalDateTime localendDateParam=LocalDateTime.fromDateFields(endDateParamTemp);
																
																// RESPON EDN DI MOVE KE JAM MULAI ISTIRAHAT 
																// jam :00:00:00 move ke jam mulai istirahat
																localendDateParam=localendDateParam.plusHours(restStart.toDate().getHours());
																localendDateParam=localendDateParam.plusMinutes(restStart.toDate().getMinutes());
																localendDateParam=localendDateParam.plusSeconds(restStart.toDate().getSeconds());
																
																endDateParam=localendDateParam;
															}
														}
													}
													validEnd=true;
												}
											break;}
			case DateTimeConstants.SUNDAY: {
											// END RESPON < END WORK
											
											if(CustomDateUtils.compareTime(new DateTime(endDateParam.toDate().getTime()), new DateTime(sourceData.getSundayFromTime().getTime()))<0){
												// set hour date startDateParam, to hour  :00:00:00 {datestartDateParam 00:00:00}
												Date endDateParamTemp=Utils.removeTime(endDateParam.toDate());
												LocalDateTime localReponEnd=LocalDateTime.fromDateFields(endDateParamTemp);
												
												// move Date with hour 00:00:00 to hour start work 
												localReponEnd=localReponEnd.plusHours(sourceData.getSundayFromTime().getHours());
												localReponEnd=localReponEnd.plusMinutes(sourceData.getSundayFromTime().getMinutes());
												localReponEnd=localReponEnd.plusSeconds(sourceData.getSundayFromTime().getSeconds());
												endDateParam=localReponEnd;
												validEnd=true;
											}else if(CustomDateUtils.compareTime(new DateTime(endDateParam.toDate().getTime()), new DateTime(sourceData.getSundayToTime().getTime()))>0){
												//respon end > end work	
												// set hour date startDateParam, to hour  :00:00:00 {datestartDateParam 00:00:00}
												Date endDateParamTemp=Utils.removeTime(endDateParam.toDate());
												LocalDateTime localReponEnd=LocalDateTime.fromDateFields(endDateParamTemp);
												// move Date with hour 00:00:00 to hour start work 
												localReponEnd=localReponEnd.plusHours(sourceData.getSundayToTime().getHours());
												localReponEnd=localReponEnd.plusMinutes(sourceData.getSundayToTime().getMinutes());
												localReponEnd=localReponEnd.plusSeconds(sourceData.getSundayToTime().getSeconds());
												endDateParam=localReponEnd;
												validEnd=true;
											}else{
												// CHECK RESPON END IN REST
												for(KPIServiceHourRest ticketRest:ticketRests){
													if(DateTimeConstants.SUNDAY==ticketRest.getDay()){
														LocalDateTime restStart=LocalDateTime.fromDateFields(ticketRest.getFromTimeRest());
														LocalDateTime restEnd=LocalDateTime.fromDateFields(ticketRest.getToTimeRest());
														if(CustomDateUtils.compareTime(new DateTime(endDateParam.toDateTime()), new DateTime(restStart.toDateTime()))>=0
																&& CustomDateUtils.compareTime(new DateTime(endDateParam.toDateTime()), new DateTime(restEnd.toDateTime()))<=0){
															Date endDateParamTemp=Utils.removeTime(endDateParam.toDate());
															LocalDateTime localendDateParam=LocalDateTime.fromDateFields(endDateParamTemp);
															
															// RESPON EDN DI MOVE KE JAM MULAI ISTIRAHAT 
															// jam :00:00:00 move ke jam mulai istirahat
															localendDateParam=localendDateParam.plusHours(restStart.toDate().getHours());
															localendDateParam=localendDateParam.plusMinutes(restStart.toDate().getMinutes());
															localendDateParam=localendDateParam.plusSeconds(restStart.toDate().getSeconds());
															
															endDateParam=localendDateParam;
														}
													}
												}
												validEnd=true;
											}
											break;}
			}
		}while(!validEnd);
		
		
		Long totalAgingInSameDays=new Long(0L);
		Long totalAgingFirstDays=new Long(0L);
		Long totalAgingBetweenDays=new Long(0L);
		Long totalAgingLastDays=new Long(0L);
		if(startDateParam.compareTo(endDateParam)<0){
              long diff =(Utils.removeTime(endDateParam.toDate()).getTime() - Utils.removeTime(startDateParam.toDate()).getTime());
              Long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
           // check apakah ada rentang hari antara startDateParam dan endDateParam
              if(days.intValue()==0){
		           // Jika selisih hari adalah 0, berarti start dan respon ada pada hari yang sama, hitung pada hari yang sama 
		           // it's mean  end - start secara langsung 
		           // karena jam istirahat berpengaruh pada aging, memperhatikan, apakah end setelah istirahat, past istirahat atau sebelum istirahat
            	   // sedangkan start sudah otomatis di atur di if paling atas
            	   // jadi tidak memperhatikan start
		           // KASUS 1
            		  switch (endDateParam.getDayOfWeek()) {
            		  		case DateTimeConstants.MONDAY: {  
		            		  			if(sourceData.getMondayFlag()==1){
			                            	  Integer includeHoliday=sourceData.getIncludeHoliday();
			                            	  //cek holiday
											  boolean isHoliday=isEndDateParamIsHolidayDate(endDateParam,includeHoliday,ticketHolidays);
											  if(isHoliday){
												  totalAgingInSameDays=0L;
											  }else{
												  Integer day=DateTimeConstants.MONDAY;
												  Integer totalSecondRest=sourceData.getSecondMondayRest();
												  totalAgingInSameDays=getTotalAgingFromSameDay(day,endDateParam,startDateParam,totalSecondRest,ticketRests);
											  }
		            		  			}else{
		            		  				totalAgingInSameDays=0L;
		            		  			}
		                          break;
		                          }
            		  		 case DateTimeConstants.TUESDAY: {
            		  			 
	            		  			if(sourceData.getTuesdayFlag()==1){
	            		  				  Integer includeHoliday=sourceData.getIncludeHoliday();
										  boolean isHoliday=isEndDateParamIsHolidayDate(endDateParam,includeHoliday,ticketHolidays);
										  if(isHoliday){
											  totalAgingInSameDays=0L;
										  }else{
		            		  			    Integer day=DateTimeConstants.TUESDAY;
		                                    Integer totalSecondRest=sourceData.getSecondTuesdayRest();
		                                    totalAgingInSameDays=getTotalAgingFromSameDay(day,endDateParam,startDateParam,totalSecondRest,ticketRests);
	            		  			      }
	            		  			}else{
	            		  				totalAgingInSameDays=0L;
	            		  			}
        		  			      break;}
            		  		 case DateTimeConstants.WEDNESDAY: {
		            		  			if(sourceData.getWednesdayFlag()==1){
		            		  				  Integer includeHoliday=sourceData.getIncludeHoliday();
											  boolean isHoliday=isEndDateParamIsHolidayDate(endDateParam,includeHoliday,ticketHolidays);
											  if(isHoliday){
												  totalAgingInSameDays=0L;
											  }else{
		            		  					  Integer day=DateTimeConstants.WEDNESDAY;
		            		  					  Integer totalSecondRest=sourceData.getSecondWednesdayRest();
		            		  					  totalAgingInSameDays=getTotalAgingFromSameDay(day,endDateParam,startDateParam,totalSecondRest,ticketRests);
		            		  				  }
		            		  			}else{
		            		  				totalAgingInSameDays=0L;
		            		  			}
                            break;}
            		  		 case DateTimeConstants.THURSDAY: {
			            		  			if(sourceData.getThursdayFlag()==1){
			            		  				  Integer includeHoliday=sourceData.getIncludeHoliday();
												  boolean isHoliday=isEndDateParamIsHolidayDate(endDateParam,includeHoliday,ticketHolidays);
												  if(isHoliday){
													  totalAgingInSameDays=0L;
												  }else{
			            		  					 Integer day=DateTimeConstants.THURSDAY;
			                                         Integer totalSecondRest=sourceData.getSecondThursdayRest();
			                                         totalAgingInSameDays=getTotalAgingFromSameDay(day,endDateParam,startDateParam,totalSecondRest,ticketRests);
			            		  				  }
			            		  			}else{
			            		  				totalAgingInSameDays=0L;
			            		  			}
                                  break;}
            		  		 
            		  		 case DateTimeConstants.FRIDAY: {
		            		  			if(sourceData.getFridayFlag()==1){
		            		  				  Integer includeHoliday=sourceData.getIncludeHoliday();
											  boolean isHoliday=isEndDateParamIsHolidayDate(endDateParam,includeHoliday,ticketHolidays);
											  if(isHoliday){
												  totalAgingInSameDays=0L;
											  }else{
		            		  					 Integer day=DateTimeConstants.FRIDAY;
		                                         Integer totalSecondRest=sourceData.getSecondFridayRest();
		                                         totalAgingInSameDays=getTotalAgingFromSameDay(day,endDateParam,startDateParam,totalSecondRest,ticketRests);
		            		  				  }
		            		  			}else{
		            		  				totalAgingInSameDays=0L;
		            		  			}
            		  			  break;}
            		  		 case DateTimeConstants.SATURDAY: {
            		  			 
		            		  			if(sourceData.getSaturdayFlag()==1){
		            		  				  Integer includeHoliday=sourceData.getIncludeHoliday();
											  boolean isHoliday=isEndDateParamIsHolidayDate(endDateParam,includeHoliday,ticketHolidays);
											  if(isHoliday){
												  totalAgingInSameDays=0L;
											  }else{
		            		  					  Integer day=DateTimeConstants.SATURDAY;
		                                          Integer totalSecondRest=sourceData.getSecondSaturdayRest();
		                                          totalAgingInSameDays=getTotalAgingFromSameDay(day,endDateParam,startDateParam,totalSecondRest,ticketRests);
		            		  				  }
		            		  			}else{
		            		  				totalAgingInSameDays=0L;
		            		  			}
            		  			 
   		  			             break;}
            		  		case DateTimeConstants.SUNDAY: {
            		  			
            		  			if(sourceData.getSundayFlag()==1){
            		  				  Integer includeHoliday=sourceData.getIncludeHoliday();
									  boolean isHoliday=isEndDateParamIsHolidayDate(endDateParam,includeHoliday,ticketHolidays);
									  if(isHoliday){
										  totalAgingInSameDays=0L;
									  }else{
          		  					     Integer day=DateTimeConstants.SUNDAY;
                                         Integer totalSecondRest=sourceData.getSecondSundayRest();
                                         totalAgingInSameDays=getTotalAgingFromSameDay(day,endDateParam,startDateParam,totalSecondRest,ticketRests);
          		  				      }
	           		  			}else{
	           		  				totalAgingInSameDays=0L;
	           		  			}
            		  			
   		  			         break;}
            		
            		  }
            		  

              		Long detik=totalAgingInSameDays;
            	    Long menit=detik/60;
            	    Long jam=menit/60;
            	    
            		System.out.println(" Detik in Same Days "+detik+" Menit "+menit+"   Jam "+jam);
              }else{
            	              // Jika rentang hari >0 
			            	  // Menghitung Hari Pertama 
			            	  // Memperhatikan Start - Sampai End. Respon End nya di isi end kerja
			            	  // JIKA HARI YANG BERBEDA , MAKA PISAHKAN PERHITUNGAN 
			            	
            	  			  // PERHITUNGAN HARI PERTAMA
			            	  LocalDateTime endDateParamForLastDay=LocalDateTime.fromDateFields(endDateParam.toDate());
			            	  LocalDateTime endDateParamForFirstDay=LocalDateTime.fromDateFields(endDateParam.toDate());
			            	  LocalDateTime startDateParamForFirstDay=LocalDateTime.fromDateFields(startDateParam.toDate());
			            	  
		            		  switch (startDateParam.getDayOfWeek()) {
			          		  		case DateTimeConstants.MONDAY: {  
				                              Integer day=DateTimeConstants.MONDAY;
				                              Integer totalSecondRest=sourceData.getSecondMondayRest();
				                              // End di isi respon start
				                              // karena tgl akhir bekerja terkadang tidak sesuai dengan 
				                              // database , jadi untuk mendapatkan hari yang sama, tgl yang sama 
				                              // dan diisi work end pada respon start
				                              //Start berbeda2, work end sama
				                              if(sourceData.getMondayFlag()==1){
						    		  			    Integer includeHoliday=sourceData.getIncludeHoliday();
												    boolean isHoliday=isTheDateIsHolidayDate(startDateParamForFirstDay, endDateParamForFirstDay, includeHoliday, ticketHolidays);
												    
												    if(isHoliday){
												    	totalAgingFirstDays=0L;
												    }else{
			                            			  Date endDateParamTemp=Utils.removeTime(startDateParam.toDate());
													  LocalDateTime localReponEnd=LocalDateTime.fromDateFields(endDateParamTemp);
														// move Date with hour 00:00:00 to hour start work 
													  localReponEnd=localReponEnd.plusHours(sourceData.getMondayToTime().getHours());
													  localReponEnd=localReponEnd.plusMinutes(sourceData.getMondayToTime().getMinutes());
													  localReponEnd=localReponEnd.plusSeconds(sourceData.getMondayToTime().getSeconds());
													  endDateParamForFirstDay=localReponEnd;
				
													  totalAgingFirstDays=calculateTotalAgingForFirstDay(day,endDateParamForFirstDay,startDateParam,totalSecondRest,ticketRests);
												    }
												}else{
				                            		totalAgingFirstDays=0L;
				                            	}
				                             break;
					                }
			          		  		case DateTimeConstants.TUESDAY: {  
							          		     if(sourceData.getTuesdayFlag()==1){
							          		    	Integer includeHoliday=sourceData.getIncludeHoliday();
												    boolean isHoliday=isTheDateIsHolidayDate(startDateParamForFirstDay, endDateParamForFirstDay, includeHoliday, ticketHolidays);
												    
												    if(isHoliday){
												    	   totalAgingFirstDays=0L;
												    }else{
							          		  			  Integer day=DateTimeConstants.TUESDAY;
							                              Integer totalSecondRest=sourceData.getSecondTuesdayRest();
							                             
							                              Date endDateParamTemp=Utils.removeTime(startDateParam.toDate());
														  LocalDateTime localReponEnd=LocalDateTime.fromDateFields(endDateParamTemp);
															// move Date with hour 00:00:00 to hour end work 
														  localReponEnd=localReponEnd.plusHours(sourceData.getTuesdayToTime().getHours());
														  localReponEnd=localReponEnd.plusMinutes(sourceData.getTuesdayToTime().getMinutes());
														  localReponEnd=localReponEnd.plusSeconds(sourceData.getTuesdayToTime().getSeconds());
														  endDateParamForFirstDay=localReponEnd;
							                              totalAgingFirstDays=calculateTotalAgingForFirstDay(day,endDateParamForFirstDay,startDateParam,totalSecondRest,ticketRests);
												    }
												}else{
							          		  		totalAgingFirstDays=0L;
							          		  	}
				                             break;
					                }
			          		  		
			          		  		case DateTimeConstants.WEDNESDAY: {  
			          		  			
				          		  		   if(sourceData.getWednesdayFlag()==1){
				          		  			    Integer includeHoliday=sourceData.getIncludeHoliday();
										        boolean isHoliday=isTheDateIsHolidayDate(startDateParamForFirstDay, endDateParamForFirstDay, includeHoliday, ticketHolidays);
										    
											    if(isHoliday){
											    	  totalAgingFirstDays=0L;
											    }else{
						          		  			  Integer day=DateTimeConstants.WEDNESDAY;
						                              Integer totalSecondRest=sourceData.getSecondWednesdayRest();
						                             
						                              Date endDateParamTemp=Utils.removeTime(startDateParam.toDate());
													  LocalDateTime localReponEnd=LocalDateTime.fromDateFields(endDateParamTemp);
														// move Date with hour 00:00:00 to hour end work 
													  localReponEnd=localReponEnd.plusHours(sourceData.getWednesdayToTime().getHours());
													  localReponEnd=localReponEnd.plusMinutes(sourceData.getWednesdayToTime().getMinutes());
													  localReponEnd=localReponEnd.plusSeconds(sourceData.getWednesdayToTime().getSeconds());
													  endDateParamForFirstDay=localReponEnd;
						                              totalAgingFirstDays=calculateTotalAgingForFirstDay(day,endDateParamForFirstDay,startDateParam,totalSecondRest,ticketRests);
										    	}
										    }else{
						          		  		totalAgingFirstDays=0L;
						          		  	}
			                             break;
				                   }
				          		  	case DateTimeConstants.THURSDAY: {  
							          		if(sourceData.getThursdayFlag()==1){
							          			    Integer includeHoliday=sourceData.getIncludeHoliday();
											        boolean isHoliday=isTheDateIsHolidayDate(startDateParamForFirstDay, endDateParamForFirstDay, includeHoliday, ticketHolidays);
											    
												    if(isHoliday){
												    	  totalAgingFirstDays=0L;
												    }else{	 
							          					  Integer day=DateTimeConstants.THURSDAY;
							                              Integer totalSecondRest=sourceData.getSecondThursdayRest();
							                              Date endDateParamTemp=Utils.removeTime(startDateParam.toDate());
														  LocalDateTime localReponEnd=LocalDateTime.fromDateFields(endDateParamTemp);
															// move Date with hour 00:00:00 to hour end work 
														  localReponEnd=localReponEnd.plusHours(sourceData.getThursdayToTime().getHours());
														  localReponEnd=localReponEnd.plusMinutes(sourceData.getThursdayToTime().getMinutes());
														  localReponEnd=localReponEnd.plusSeconds(sourceData.getThursdayToTime().getSeconds());
														  endDateParamForFirstDay=localReponEnd;
							                              totalAgingFirstDays=calculateTotalAgingForFirstDay(day,endDateParamForFirstDay,startDateParam,totalSecondRest,ticketRests);
												    }
										    }else{
							          			totalAgingFirstDays=0L;
							          		}
				          		  		
			                              break;
				                   }
				          		  	
					          		 case DateTimeConstants.FRIDAY: {  
								            if(sourceData.getFridayFlag()==1){
								            	Integer includeHoliday=sourceData.getIncludeHoliday();
										        boolean isHoliday=isTheDateIsHolidayDate(startDateParamForFirstDay, endDateParamForFirstDay, includeHoliday, ticketHolidays);
										    
											    if(isHoliday){
											    	  totalAgingFirstDays=0L;
											    }else{
									          		  Integer day=DateTimeConstants.FRIDAY;
						                              Integer totalSecondRest=sourceData.getSecondFridayRest();
						                             
						                              Date endDateParamTemp=Utils.removeTime(startDateParam.toDate());
													  LocalDateTime localReponEnd=LocalDateTime.fromDateFields(endDateParamTemp);
														// move Date with hour 00:00:00 to hour end work 
													  localReponEnd=localReponEnd.plusHours(sourceData.getFridayToTime().getHours());
													  localReponEnd=localReponEnd.plusMinutes(sourceData.getFridayToTime().getMinutes());
													  localReponEnd=localReponEnd.plusSeconds(sourceData.getFridayToTime().getSeconds());
													  endDateParamForFirstDay=localReponEnd;
						                              totalAgingFirstDays=calculateTotalAgingForFirstDay(day,endDateParamForFirstDay,startDateParam,totalSecondRest,ticketRests);
											    }
											}else{
								          		totalAgingFirstDays=0L;
								          	}
					          			 break;
				                   }
					          		 
					          		 case DateTimeConstants.SATURDAY: {  
					          			 
					          			    if(sourceData.getSaturdayFlag()==1){
					          			    	Integer includeHoliday=sourceData.getIncludeHoliday();
										        boolean isHoliday=isTheDateIsHolidayDate(startDateParamForFirstDay, endDateParamForFirstDay, includeHoliday, ticketHolidays);
										    
											    if(isHoliday){
											    	  totalAgingFirstDays=0L;
											    }else{
									          		  Integer day=DateTimeConstants.SATURDAY;
						                              Integer totalSecondRest=sourceData.getSecondSaturdayRest();
						                              Date endDateParamTemp=Utils.removeTime(startDateParam.toDate());
													  LocalDateTime localReponEnd=LocalDateTime.fromDateFields(endDateParamTemp);
														// move Date with hour 00:00:00 to hour end work 
													  localReponEnd=localReponEnd.plusHours(sourceData.getSaturdayToTime().getHours());
													  localReponEnd=localReponEnd.plusMinutes(sourceData.getSaturdayToTime().getMinutes());
													  localReponEnd=localReponEnd.plusSeconds(sourceData.getSaturdayToTime().getSeconds());
													  endDateParamForFirstDay=localReponEnd;
						                              totalAgingFirstDays=calculateTotalAgingForFirstDay(day,endDateParamForFirstDay,startDateParam,totalSecondRest,ticketRests);
											    }
										   }else{
								          		totalAgingFirstDays=0L;
								          	}
			                              break;
				                   }
					          		 case DateTimeConstants.SUNDAY: {  
					          			 if(sourceData.getSundayFlag()==1){
					          				Integer includeHoliday=sourceData.getIncludeHoliday();
									        boolean isHoliday=isTheDateIsHolidayDate(startDateParamForFirstDay, endDateParamForFirstDay, includeHoliday, ticketHolidays);
									    
										    if(isHoliday){
										    	  totalAgingFirstDays=0L;
										    }else{
					          					  Integer day=DateTimeConstants.SUNDAY;
					                              Integer totalSecondRest=sourceData.getSecondSundayRest();
					                              Date endDateParamTemp=Utils.removeTime(startDateParam.toDate());
												  LocalDateTime localReponEnd=LocalDateTime.fromDateFields(endDateParamTemp);
													// move Date with hour 00:00:00 to hour end work 
												  localReponEnd=localReponEnd.plusHours(sourceData.getSundayToTime().getHours());
												  localReponEnd=localReponEnd.plusMinutes(sourceData.getSundayToTime().getMinutes());
												  localReponEnd=localReponEnd.plusSeconds(sourceData.getSundayToTime().getSeconds());
												  endDateParamForFirstDay=localReponEnd;
					                              totalAgingFirstDays=calculateTotalAgingForFirstDay(day,endDateParamForFirstDay,startDateParam,totalSecondRest,ticketRests);
										    }
										}else{
				          					totalAgingFirstDays=0L;
				          				}
			                            break;
				                   }
		            		  }
		            		  Long detikAgingFisrtDay=totalAgingFirstDays;
		            		  Long menitAgingFisrtDay=detikAgingFisrtDay/60;
		            		  Long jamAgingFisrtDay=menitAgingFisrtDay/60;
		            		  
		            		  System.out.println("\n First Day : Detik "+detikAgingFisrtDay +"Menit :  "+menitAgingFisrtDay+" Jam  : "+jamAgingFisrtDay);
		            		  //=========================================
		            		  //END HARI PERTAMA
		            		  //==========================================
        		  
			            	  // START CALCULATE BETWEEN DAYS 
			            	  // cek jika diantara tanggal tengah ada yang libur atau 
		            		  System.out.println("Rentang Hari "+days);
		            		  System.out.println("======================");
		            		  System.out.println("Start Between Days");
		            		  Long tempBetweenDays=new Long(0L);
			            	  for (int i=1; i<days; i++) {
								  LocalDateTime curentStartWork=startDateParam.plusDays(i);
								  if(curentStartWork.getDayOfWeek()==DateTimeConstants.MONDAY){
									  if(sourceData.getMondayFlag()==1){
										  Integer includeHoliday=sourceData.getIncludeHoliday();
										  boolean isHoliday=isTheDateIsHolidayDate(curentStartWork, endDateParamForFirstDay, includeHoliday, ticketHolidays);
										  if(isHoliday){
											  tempBetweenDays=0L;
										  }else{
											  tempBetweenDays=Long.valueOf(sourceData.getMondayWorkSec());
										  }
									  }else{
										  tempBetweenDays=0L;
									  }
								  }else if(curentStartWork.getDayOfWeek()==DateTimeConstants.TUESDAY){
									  if(sourceData.getTuesdayFlag()==1){
										  Integer includeHoliday=sourceData.getIncludeHoliday();
										  boolean isHoliday=isTheDateIsHolidayDate(curentStartWork, endDateParamForFirstDay, includeHoliday, ticketHolidays);
										  if(isHoliday){
											  tempBetweenDays=0L;
										  }else{
											  tempBetweenDays=Long.valueOf(sourceData.getTuesdayWorkSec());
										  }
									  }else{
										  tempBetweenDays=0L;
									  }
								  }else if(curentStartWork.getDayOfWeek()==DateTimeConstants.WEDNESDAY){
									  if(sourceData.getWednesdayFlag()==1){
										  Integer includeHoliday=sourceData.getIncludeHoliday();
										  boolean isHoliday=isTheDateIsHolidayDate(curentStartWork, endDateParamForFirstDay, includeHoliday, ticketHolidays);
										  if(isHoliday){
											  tempBetweenDays=0L;
										  }else{
											  tempBetweenDays=Long.valueOf(sourceData.getWednesdayWorkSec());
										  }
									  }else{
										  tempBetweenDays=0L;
									  }
								  }else if(curentStartWork.getDayOfWeek()==DateTimeConstants.THURSDAY){
									  if(sourceData.getThursdayFlag()==1){
										  Integer includeHoliday=sourceData.getIncludeHoliday();
										  boolean isHoliday=isTheDateIsHolidayDate(curentStartWork, endDateParamForFirstDay, includeHoliday, ticketHolidays);
										  if(isHoliday){
											  tempBetweenDays=0L;
										  }else{
											  tempBetweenDays=Long.valueOf(sourceData.getThursdayWorkSec());
										  }
									  }else{
										  tempBetweenDays=0L;
									  }
								  }else if(curentStartWork.getDayOfWeek()==DateTimeConstants.FRIDAY){
									  if(sourceData.getFridayFlag()==1){
										  Integer includeHoliday=sourceData.getIncludeHoliday();
										  boolean isHoliday=isTheDateIsHolidayDate(curentStartWork, endDateParamForFirstDay, includeHoliday, ticketHolidays);
										  if(isHoliday){
											  tempBetweenDays=0L;
										  }else{
											  tempBetweenDays=Long.valueOf(sourceData.getFridayWorkSec());
										  }
									  }else{
										  tempBetweenDays=0L;
									  }
								  }else if(curentStartWork.getDayOfWeek()==DateTimeConstants.SATURDAY){
									  if(sourceData.getSaturdayFlag()==1){
										  Integer includeHoliday=sourceData.getIncludeHoliday();
										  boolean isHoliday=isTheDateIsHolidayDate(curentStartWork, endDateParamForFirstDay, includeHoliday, ticketHolidays);
										  if(isHoliday){
											  tempBetweenDays=0L;
										  }else{
											  tempBetweenDays=Long.valueOf(sourceData.getSaturdayWorkSec());
										  }
									  }else{
										  tempBetweenDays=0L;
									  }
								  }else if(curentStartWork.getDayOfWeek()==DateTimeConstants.SUNDAY){
									  if(sourceData.getSundayFlag()==1){
										  Integer includeHoliday=sourceData.getIncludeHoliday();
										  boolean isHoliday=isTheDateIsHolidayDate(curentStartWork, endDateParamForFirstDay, includeHoliday, ticketHolidays);
										  if(isHoliday){
											  tempBetweenDays=0L;
										  }else{
											  tempBetweenDays=Long.valueOf(sourceData.getSundayWorkSec());
										  }
									  }else{
										  tempBetweenDays=0L;
									  }
								  }
								  
								  System.out.println("Hari Ke "+i+"  "+tempBetweenDays);
								  totalAgingBetweenDays=totalAgingBetweenDays+tempBetweenDays;
								  
							  }
			            	  
			            	System.out.println("End Between Days.... ");
			            	  // END CALCULATE BETWEEN DAYS 
			            	  
			            	    Long detikBetweenDays=totalAgingBetweenDays;
			          			Long menitBetweenDay=detikBetweenDays/60;
			          			Long jamBetweenDay=menitBetweenDay/60;
			          			
			          		System.out.println("\n Detik between days :"+detikBetweenDays+"  Menit : "+menitBetweenDay+" Jam : "+jamBetweenDay);
            	 
			            	    
			      // CALCULATE LAST DAYS
        		  // yang harus di ke defaul adalah hari starnya
        		  // karena akan mengambil waktu dari end - start, waktu pada hari itu
        		  
        		  // RESPON_START=REMOVE_TGL(RESPON_END)+JAM,MENIT,DETIK START WORK (untuk memastikan tanggal sama)
        		  // TERKADANG TANGGAL START WORK DI DATABASE TANGGALNYA BERBEDA , MISAL TANGGAL 2016-02-02, padahal ticket 2017
        		  switch (endDateParamForLastDay.getDayOfWeek()) {
    		  		case DateTimeConstants.MONDAY: {  
                        
    		  					if(sourceData.getMondayFlag()==1){
    		  						  Integer includeHoliday=sourceData.getIncludeHoliday();
									  boolean isHoliday=isTheDateIsHolidayDate(endDateParamForLastDay, endDateParamForFirstDay, includeHoliday, ticketHolidays);
									  if(isHoliday){
										  tempBetweenDays=0L;
									  }else{
										  Integer day=DateTimeConstants.MONDAY;
										  Integer totalSecondRest=sourceData.getSecondMondayRest();
										  
										  Date startDateParamTemp=Utils.removeTime(endDateParamForLastDay.toDate());
										  LocalDateTime localReponStart=LocalDateTime.fromDateFields(startDateParamTemp);
										  
										  // move Date with hour 00:00:00 to hour start work 
										  localReponStart=localReponStart.plusHours(sourceData.getMondayFromTime().getHours());
										  localReponStart=localReponStart.plusMinutes(sourceData.getMondayFromTime().getMinutes());
										  localReponStart=localReponStart.plusSeconds(sourceData.getMondayFromTime().getSeconds());
										  startDateParam=localReponStart;
										  totalAgingLastDays=getTotalAgingFromLastDay(day,endDateParamForLastDay,startDateParam,totalSecondRest,ticketRests);
									  }
    		  					}else{
    		  						totalAgingLastDays=0L;
    		  					}
	                          break;
	                }
    		  		case DateTimeConstants.TUESDAY: {  
    		  			if(sourceData.getTuesdayFlag()==1){
    		  				  Integer includeHoliday=sourceData.getIncludeHoliday();
							  boolean isHoliday=isTheDateIsHolidayDate(endDateParamForLastDay, endDateParamForFirstDay, includeHoliday, ticketHolidays);
							  if(isHoliday){
								  tempBetweenDays=0L;
							  }else{
	        		  			  Integer day=DateTimeConstants.TUESDAY;
	                              Integer totalSecondRest=sourceData.getSecondTuesdayRest();
	
	                              Date startDateParamTemp=Utils.removeTime(endDateParamForLastDay.toDate());
								  LocalDateTime localReponStart=LocalDateTime.fromDateFields(startDateParamTemp);
									
									// move Date with hour 00:00:00 to hour start work 
								  localReponStart=localReponStart.plusHours(sourceData.getTuesdayFromTime().getHours());
								  localReponStart=localReponStart.plusMinutes(sourceData.getTuesdayFromTime().getMinutes());
								  localReponStart=localReponStart.plusSeconds(sourceData.getTuesdayFromTime().getSeconds());
	                             startDateParam=localReponStart;
	                             
	                             totalAgingLastDays=getTotalAgingFromLastDay(day,endDateParamForLastDay,startDateParam,totalSecondRest,ticketRests);
							  }
					    }else{
	  						totalAgingLastDays=0L;
	  					}
	                    break;
	                }
    		  		
    		  		case DateTimeConstants.WEDNESDAY: { 
    		  			if(sourceData.getWednesdayFlag()==1){
        		  			 
    		  				boolean isIncludeHoliday=false;
    		  			    Integer includeHoliday=sourceData.getIncludeHoliday();
						    boolean isHoliday=isTheDateIsHolidayDate(endDateParamForLastDay, endDateParamForFirstDay, includeHoliday, ticketHolidays);
						    if(isHoliday){
							      tempBetweenDays=0L;
						   }else{
        		  				  Integer day=DateTimeConstants.WEDNESDAY;
	                              Integer totalSecondRest=sourceData.getSecondWednesdayRest();
	
	                              Date startDateParamTemp=Utils.removeTime(endDateParamForLastDay.toDate());
								  LocalDateTime localReponStart=LocalDateTime.fromDateFields(startDateParamTemp);
									
									// move Date with hour 00:00:00 to hour start work 
								  localReponStart=localReponStart.plusHours(sourceData.getWednesdayFromTime().getHours());
								  localReponStart=localReponStart.plusMinutes(sourceData.getWednesdayFromTime().getMinutes());
								  localReponStart=localReponStart.plusSeconds(sourceData.getWednesdayFromTime().getSeconds());
	                             startDateParam=localReponStart;
	                             
                                 totalAgingLastDays=getTotalAgingFromLastDay(day,endDateParamForLastDay,startDateParam,totalSecondRest,ticketRests);
							  }
    		  			 }else{
		  						totalAgingLastDays=0L;
		  					}
                        break;
                   }
          		  	case DateTimeConstants.THURSDAY: {
					          		 if(sourceData.getThursdayFlag()==1){
					          			boolean isIncludeHoliday=false;
			    		  			    Integer includeHoliday=sourceData.getIncludeHoliday();
									    boolean isHoliday=isTheDateIsHolidayDate(endDateParamForLastDay, endDateParamForFirstDay, includeHoliday, ticketHolidays);
									    if(isHoliday){
										      tempBetweenDays=0L;
									    }else{		
						          		  	 Integer day=DateTimeConstants.THURSDAY;
					                         Integer totalSecondRest=sourceData.getSecondThursdayRest();
					
					                         Date startDateParamTemp=Utils.removeTime(endDateParamForLastDay.toDate());
											 LocalDateTime localReponStart=LocalDateTime.fromDateFields(startDateParamTemp);
												
											// move Date with hour 00:00:00 to hour start work 
											  localReponStart=localReponStart.plusHours(sourceData.getThursdayFromTime().getHours());
											  localReponStart=localReponStart.plusMinutes(sourceData.getThursdayFromTime().getMinutes());
											  localReponStart=localReponStart.plusSeconds(sourceData.getThursdayFromTime().getSeconds());
					                         startDateParam=localReponStart;
				                        
					                         totalAgingLastDays=getTotalAgingFromLastDay(day,endDateParamForLastDay,startDateParam,totalSecondRest,ticketRests);
										  }
									}else{
		 		  						totalAgingLastDays=0L;
		 		  					}
		                         break;
		                         }
          		  	
				          		 case DateTimeConstants.FRIDAY: { 
				          			if(sourceData.getFridayFlag()==1){
				          				boolean isIncludeHoliday=false;
			    		  			    Integer includeHoliday=sourceData.getIncludeHoliday();
									    boolean isHoliday=isTheDateIsHolidayDate(endDateParamForLastDay, endDateParamForFirstDay, includeHoliday, ticketHolidays);
									    if(isHoliday){
										      tempBetweenDays=0L;
									    }else{	
						          			  Integer day=DateTimeConstants.FRIDAY;
					                          Integer totalSecondRest=sourceData.getSecondFridayRest();
					
					                          Date startDateParamTemp=Utils.removeTime(endDateParamForLastDay.toDate());
											  LocalDateTime localReponStart=LocalDateTime.fromDateFields(startDateParamTemp);
												
												// move Date with hour 00:00:00 to hour start work 
											  localReponStart=localReponStart.plusHours(sourceData.getFridayFromTime().getHours());
											  localReponStart=localReponStart.plusMinutes(sourceData.getFridayFromTime().getMinutes());
											  localReponStart=localReponStart.plusSeconds(sourceData.getFridayFromTime().getSeconds());
					                          startDateParam=localReponStart;
					                        
					                          totalAgingLastDays=getTotalAgingFromLastDay(day,endDateParamForLastDay,startDateParam,totalSecondRest,ticketRests);
										  }
								    }else{
		 		  						totalAgingLastDays=0L;
		 		  					}
			                        break;
			                   }
	          		 
			          		 case DateTimeConstants.SATURDAY: {  
			          			       if(sourceData.getSaturdayFlag()==1){
				          			    	boolean isIncludeHoliday=false;
				    		  			    Integer includeHoliday=sourceData.getIncludeHoliday();
										    boolean isHoliday=isTheDateIsHolidayDate(endDateParamForLastDay, endDateParamForFirstDay, includeHoliday, ticketHolidays);
										    
										    if(isHoliday){
											      tempBetweenDays=0L;
										    }else{
							          			  Integer day=DateTimeConstants.SATURDAY;
						                          Integer totalSecondRest=sourceData.getSecondSaturdayRest();
						
						                          Date startDateParamTemp=Utils.removeTime(endDateParamForLastDay.toDate());
												  LocalDateTime localReponStart=LocalDateTime.fromDateFields(startDateParamTemp);
													
													// move Date with hour 00:00:00 to hour start work 
												  localReponStart=localReponStart.plusHours(sourceData.getSaturdayFromTime().getHours());
												  localReponStart=localReponStart.plusMinutes(sourceData.getSaturdayFromTime().getMinutes());
												  localReponStart=localReponStart.plusSeconds(sourceData.getSaturdayFromTime().getSeconds());
						                          startDateParam=localReponStart;
						                        
						                          totalAgingLastDays=getTotalAgingFromLastDay(day,endDateParamForLastDay,startDateParam,totalSecondRest,ticketRests);
											}
								    }else{
		 		  						totalAgingLastDays=0L;
		 		  					}
		                          break;
		                   }
		          		 case DateTimeConstants.SUNDAY: {  
		          			 if(sourceData.getSaturdayFlag()==1){
		          				boolean isIncludeHoliday=false;
	    		  			    Integer includeHoliday=sourceData.getIncludeHoliday();
							    boolean isHoliday=isTheDateIsHolidayDate(endDateParamForLastDay, endDateParamForFirstDay, includeHoliday, ticketHolidays);
							    
							    if(isHoliday){
								      tempBetweenDays=0L;
							    }else{
				          			  Integer day=DateTimeConstants.SUNDAY;
			                          Integer totalSecondRest=sourceData.getSecondSundayRest();
			
			                          Date startDateParamTemp=Utils.removeTime(endDateParamForLastDay.toDate());
									  LocalDateTime localReponStart=LocalDateTime.fromDateFields(startDateParamTemp);
										
										// move Date with hour 00:00:00 to hour start work 
									  localReponStart=localReponStart.plusHours(sourceData.getSundayFromTime().getHours());
									  localReponStart=localReponStart.plusMinutes(sourceData.getSundayFromTime().getMinutes());
									  localReponStart=localReponStart.plusSeconds(sourceData.getSundayFromTime().getSeconds());
			                          startDateParam=localReponStart;
			                        
			                          totalAgingLastDays=getTotalAgingFromLastDay(day,endDateParamForLastDay,startDateParam,totalSecondRest,ticketRests);
								  }
							 }else{
 		  						totalAgingLastDays=0L;
 		  					 }
	                          break;
	                   }
        		  }
        		  
	            		//END CALCULATE LAST DAYS
	            		//END CALCULATE LAST DAYS
	            		//END CALCULATE LAST DAYS
        		  
              }  // END IF DAYS>0
		}else{
			//Jika end>start 
			totalAging=0L;
		}
		
		Long detik=totalAgingLastDays;
		Long menit=detik/60;
		System.out.println(" \n ==============================  \n Last Day  : "+detik+"  Menit Last day : "+menit+" Jam : "+menit/60);
		System.out.println("=================================\n");
		System.out.println(" \n Respon End : "+endDateParam+" \n\n totalAgingInSameDays   :"+totalAgingInSameDays+"  totalAgingFirstDays   :"+totalAgingFirstDays+"  totalAgingBetweenDays   :"+totalAgingBetweenDays+" totalAgingLastDays   :"+totalAgingLastDays);
		
		
		totalAging=totalAgingInSameDays+totalAgingFirstDays+totalAgingBetweenDays+totalAgingLastDays;
		return totalAging;
	}

	/***
	 * Default database includeHoliday=0, jika 1 maka hari libur dianggap masuk
	 * */
	public boolean isEndDateParamIsHolidayDate(LocalDateTime endDateParam, Integer includeHoliday,
			List<ITSMTicketHolidayDate> ticketHolidays) {
			boolean isHoliday=false;
			for(ITSMTicketHolidayDate ticketHoliday:ticketHolidays){
				Date holiday=Utils.removeTime(ticketHoliday.getHolidayDate());
				Date toDay=Utils.removeTime(endDateParam.toDate());
				if(holiday.compareTo(toDay)==0){
					if(includeHoliday==0){
						isHoliday=true;
					}
				}
				if(isHoliday)break;
			}
			return isHoliday;
	}

	public boolean isTheDateIsHolidayDate(LocalDateTime startDateParam, LocalDateTime endDateParam, Integer includeHoliday,
		List<ITSMTicketHolidayDate> ticketHolidays) {
	    boolean isHoliday=false;
		for(ITSMTicketHolidayDate ticketHoliday:ticketHolidays){
			Date holiday=Utils.removeTime(ticketHoliday.getHolidayDate());
			Date toDay=Utils.removeTime(startDateParam.toDate());
			if(holiday.compareTo(toDay)==0){
				if(includeHoliday==0){
					isHoliday=true;
				}
			}
			if(isHoliday)break;
		}
		return isHoliday;
	}

	public Long getTotalAgingFromLastDay(Integer day, LocalDateTime endDateParam, LocalDateTime startDateParam,
			Integer totalSecondRest, List<KPIServiceHourRest> ticketRests) {
		  Long totalAging=new Long(0);
	   	  Long totalAgingOnRest=0L;
	   	  for(KPIServiceHourRest ticketRest:ticketRests){
	   		  if(day==ticketRest.getDay()){
	   			  LocalDateTime restStart=LocalDateTime.fromDateFields(ticketRest.getFromTimeRest());
		              LocalDateTime restEnd=LocalDateTime.fromDateFields(ticketRest.getToTimeRest());
		              if(CustomDateUtils.compareTime(new DateTime(endDateParam.toDateTime()), new DateTime(restStart.toDateTime()))>=0
		            		  && CustomDateUtils.compareTime(new DateTime(endDateParam.toDateTime()), new DateTime(restEnd.toDateTime()))>=0)
		              {
		            	  totalAgingOnRest+=secondTotalAgingBetweenDate(restEnd.toDate().getTime(), restStart.toDate().getTime(), 0L);
		              }
	   		  }
	   	  }
	   	  totalAging=secondTotalAgingBetweenDate(endDateParam.toDate().getTime(),startDateParam.toDate().getTime(),totalAgingOnRest);
          return totalAging;
	}

	//dari start respon ke end work
	//yang diperhatikan start respon
	//end respon diisi default jam akhir bekerja
	 public Long calculateTotalAgingForFirstDay(Integer day, LocalDateTime endDateParam, LocalDateTime startDateParam,
			Integer totalSecondRest, List<KPIServiceHourRest> ticketRests) {
/*		 
 *       9:02-8:29-restAging
 *		 8:29:00,9:10:00,
 *		 8:30:00, 9:00
 *		 Istirahat jam 8:30-9:00  
 *		 long restAging=0L<
 *	    
 *	     if(startDateParam<restStart && startDateParam<restEnd)
 *	     {
 *	    		8:29<8:30      Y, 8:29<9:00       Y
 *	    		restAging=9:00:00-8:30:00
 *	 	  	    8:29<11:30     Y, 8:29<1:00:00   Y
 *	    		restAging=9:00:00-8:30:00
 *
 *	    		9:10<8:29  N, 9:10<9:00 N
 *	    		break;
 *	    		8:29<11:30    Y, 8:29<1:00 Y
 *	    }
 */
		  Long totalAging=new Long(0);
	   	  Long totalAgingOnRest=0L;
	   	  for(KPIServiceHourRest ticketRest:ticketRests){
	   		  if(day==ticketRest.getDay()){
	   			  LocalDateTime restStart=LocalDateTime.fromDateFields(ticketRest.getFromTimeRest());
		              LocalDateTime restEnd=LocalDateTime.fromDateFields(ticketRest.getToTimeRest());
		              if(CustomDateUtils.compareTime(new DateTime(startDateParam.toDateTime()), new DateTime(restStart.toDateTime()))<=0
		            		  && CustomDateUtils.compareTime(new DateTime(startDateParam.toDateTime()), new DateTime(restEnd.toDateTime()))<=0){
		            	  totalAgingOnRest+=secondTotalAgingBetweenDate(restEnd.toDate().getTime(), restStart.toDate().getTime(), 0L);
		              }
	   		  }
	   	  }
	   	  totalAging=secondTotalAgingBetweenDate(endDateParam.toDate().getTime(),startDateParam.toDate().getTime(),totalAgingOnRest);
          return totalAging;
	}

	public Long getTotalAgingFromSameDay(Integer day, LocalDateTime endDateParam, LocalDateTime startDateParam,
			Integer totalSecondRest, List<KPIServiceHourRest> ticketRests) {
	    /**  Logika perhitungan di hari yang sama 
		  *		8:29,8:00,9:00
		  *		    istirahat 1 jam 8:30-9:00              Y        Y      Y
		  *		    istirahat 2 jam 11:30-1:00             N        Y      Y       
		  *	    9:01,17:00
			*	// setelah istirahat
			*	long restAging
			*	if(endDateParam=>restEnd && startDateParam<=rstStart){
			*		 1
			*		   10 >9 ? Y ,  9:02< 8:30 N
			*		      break;
			*		 2.
			*		   10 >1:00  N, 10 <11:30 Y
			*		      break 
			*	     3.
			*	       17>9:00  Y , 8 < 8:30  Y
			*	          restAging=9:00 - 8:30
			*	       17:00 >1 Y, 8:02<rstStart Y
			*	          restAging=1-11:30
			*	     4
			*	        9:01=>9  Y, 8:30<=8:30
			*	}
			*	9:01-8:30-restAging
			*	31 
	     * */ 
		  Long totalAging=new Long(0);
    	  Long totalAgingOnRest=0L;
    	  
    	  
    	  for(KPIServiceHourRest ticketRest:ticketRests){
    		  if(day==ticketRest.getDay()){
    			  LocalDateTime restStart=LocalDateTime.fromDateFields(ticketRest.getFromTimeRest());
	              LocalDateTime restEnd=LocalDateTime.fromDateFields(ticketRest.getToTimeRest());
	              
	              
	              if(CustomDateUtils.compareTime(new DateTime(endDateParam.toDateTime()), new DateTime(restEnd.toDateTime()))>=0
	            		  && CustomDateUtils.compareTime(new DateTime(startDateParam.toDateTime()), new DateTime(restStart.toDateTime()))<=0){
	            	  totalAgingOnRest+=secondTotalAgingBetweenDate(restEnd.toDate().getTime(), restStart.toDate().getTime(), 0L);
	              }
    		  }
    	  }
    	  totalAging=secondTotalAgingBetweenDate(endDateParam.toDate().getTime(),startDateParam.toDate().getTime(),totalAgingOnRest);
          return totalAging;
	}

	public Long secondTotalAgingBetweenDate(Long endRespon, Long startRespon, Long timeRest) {
		
		 Long secondRespon=(endRespon-startRespon);
		 Long secondRest=timeRest;
		 Long timeRespon=TimeUnit.MILLISECONDS.toSeconds(secondRespon)-secondRest;
		 return timeRespon;
	}

	
	public Integer getTotalDay(LocalDateTime startDate,LocalDateTime endDate, List<ITSMTicketHolidayDate> holidays){
		 boolean validStart = false;
	        Long totalDays = new Long(0L);
	        
	        LocalDateTime tmp;
	        
	        if(startDate.compareTo(endDate)>0){
	        	tmp=startDate;
	        	//temp berisi start
	        	startDate=endDate;
	        	endDate=tmp;
	        }
	        Integer sumOfDays = 0;
	        do {
	            switch (startDate.getDayOfWeek()) {
	                case DateTimeConstants.MONDAY: {
	                    
	                     boolean isHoliday=isHolidayDate(startDate,startDate,holidays);
	                     if(isHoliday){
	                        validStart=true;
	                        Date actualDate = startDate.toDate();
	                        startDate = LocalDateTime.fromDateFields(actualDate).plusDays(1);
	                     }else{
	                        validStart=true;
	                        Date actualDate = startDate.toDate();
	                        startDate = LocalDateTime.fromDateFields(actualDate).plusDays(1);
	                        sumOfDays += 1;
	                     }       
	                    break;
	                }
	                case DateTimeConstants.TUESDAY: {
	                    
	                   boolean isHoliday=isHolidayDate(startDate,startDate,holidays);
	                     if(isHoliday){
	                        validStart=true;
	                        Date actualDate = startDate.toDate();
	                        startDate = LocalDateTime.fromDateFields(actualDate).plusDays(1);
	                     }else{
	                        validStart=true;
	                        Date actualDate = startDate.toDate();
	                        startDate = LocalDateTime.fromDateFields(actualDate).plusDays(1);
	                        sumOfDays += 1;
	                     }       
	                    break;
	                }
	                case DateTimeConstants.WEDNESDAY: {
	                    
	                    boolean isHoliday=isHolidayDate(startDate,startDate,holidays);
	                     if(isHoliday){
	                        validStart=true;
	                        Date actualDate = startDate.toDate();
	                        startDate = LocalDateTime.fromDateFields(actualDate).plusDays(1);
	                     }else{
	                        validStart=true;
	                        Date actualDate = startDate.toDate();
	                        startDate = LocalDateTime.fromDateFields(actualDate).plusDays(1);
	                        sumOfDays += 1;
	                     }       
	                    break;
	                }
	                case DateTimeConstants.THURSDAY: {
	                    
	                    boolean isHoliday=isHolidayDate(startDate,startDate,holidays);
	                     if(isHoliday){
	                        validStart=true;
	                        Date actualDate = startDate.toDate();
	                        startDate = LocalDateTime.fromDateFields(actualDate).plusDays(1);
	                     }else{
	                        validStart=true;
	                        Date actualDate = startDate.toDate();
	                        startDate = LocalDateTime.fromDateFields(actualDate).plusDays(1);
	                        sumOfDays += 1;
	                     }       
	                    break;
	                }
	                case DateTimeConstants.FRIDAY: {
	                    boolean isHoliday=isHolidayDate(startDate,startDate,holidays);
	                     if(isHoliday){
	                        validStart=true;
	                        Date actualDate = startDate.toDate();
	                        startDate = LocalDateTime.fromDateFields(actualDate).plusDays(1);
	                     }else{
	                        validStart=true;
	                        Date actualDate = startDate.toDate();
	                        startDate = LocalDateTime.fromDateFields(actualDate).plusDays(1);
	                        sumOfDays += 1;
	                     }       
	                    break;
	                }
	                case DateTimeConstants.SATURDAY: {
	                	validStart=true;
	                    Date actualDate = startDate.toDate();
	                    startDate = LocalDateTime.fromDateFields(actualDate).plusDays(1);
	                    break;
	                }
	                case DateTimeConstants.SUNDAY: {
	                    validStart=true;
	                    Date actualDate = startDate.toDate();
	                    startDate = LocalDateTime.fromDateFields(actualDate).plusDays(1);
	                    break;
	                }
	            }
	            if (startDate.compareTo(endDate) > 0) {
	                break;
	            }
	        } while (validStart);

	        return sumOfDays;
	}
	

public boolean isHolidayDate(LocalDateTime startDateParam, LocalDateTime endDateParam, 
    List<ITSMTicketHolidayDate> holidays) {
      boolean isHoliday=false;
    for(ITSMTicketHolidayDate ticketHoliday:holidays){
      Date holiday=Utils.removeTime(ticketHoliday.getHolidayDate());
      Date toDay=Utils.removeTime(startDateParam.toDate());
      if(holiday.compareTo(toDay)==0){
          isHoliday=true;
      }
      if(isHoliday)break;
    }
    return isHoliday;
}

	
}
