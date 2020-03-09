package com.axelor.admission.module;

import com.axelor.admission.service.AdmissionProcessService;
import com.axelor.admission.service.AdmissionProcessServiceImpl;
import com.axelor.app.AxelorModule;

public class AdmissionModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(AdmissionProcessService.class).to(AdmissionProcessServiceImpl.class);
  }
}
