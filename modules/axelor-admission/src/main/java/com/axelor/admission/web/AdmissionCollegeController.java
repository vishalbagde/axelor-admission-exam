package com.axelor.admission.web;

import com.axelor.admission.db.AdmissionEntry;
import com.axelor.admission.db.College;
import com.axelor.admission.db.repo.CollegeRepository;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class AdmissionCollegeController {
	
	public void setCollege(ActionRequest request,ActionResponse response)
	{
         College college = request.getContext().asType(College.class);
		 AdmissionEntry admissionEntry =request.getContext().getParent().asType(AdmissionEntry.class);
		 		
		 
		 //Beans.get(CollegeRepository.class).all().;
		 
		 
		 
	}

}
