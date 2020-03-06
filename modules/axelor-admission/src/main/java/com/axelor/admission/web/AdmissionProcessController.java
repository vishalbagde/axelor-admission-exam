package com.axelor.admission.web;


import com.axelor.admission.db.AdmissionEntry;
import com.axelor.admission.db.AdmissionProcess;
import com.axelor.admission.service.AdmissionProcessService;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;


public class AdmissionProcessController {
	
	public void admissionProcess(ActionRequest request,ActionResponse response)
	{
		AdmissionProcess admissionProcess = request.getContext().asType(AdmissionProcess.class);
		
		Beans.get(AdmissionProcessService.class).computeAdmissionProcess(admissionProcess);
		response.setFlash("Admission Process Completed");
		
		response.setView(ActionView.define("Admission List").model(AdmissionEntry.class.getName())
				.add("grid", "admission-entry-grid").map());
	}
}