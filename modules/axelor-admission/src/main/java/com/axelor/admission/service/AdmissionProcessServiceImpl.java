package com.axelor.admission.service;

import com.axelor.admission.db.AdmissionEntry;
import com.axelor.admission.db.AdmissionProcess;
import com.axelor.admission.db.CollegeEntry;
import com.axelor.admission.db.FacultyEntry;
import com.axelor.admission.db.repo.AdmissionEntryRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AdmissionProcessServiceImpl implements AdmissionProcessService {

	@Inject
	AdmissionEntryRepository admissionEntryRepo;

	@Transactional
	@Override
	public void computeAdmissionProcess(AdmissionProcess admissionProcess) {

		LocalDate fromDate = admissionProcess.getFromDate();
		LocalDate toDate = admissionProcess.getToDate();

		List<AdmissionEntry> admissionEntryList = admissionEntryRepo.all()
				.filter("self.registrationDate BETWEEN ?1 AND ?2 AND self.statusSelect = ?3 ", fromDate, toDate,
						admissionEntryRepo.ADMISSION_ENTRY_STATUS_CONNFIRMED)
				.fetch();

		// sort admission List base on merit
		admissionEntryList = sortAdmissonEntryInDecendingOrder(admissionEntryList);

		// admissionEntryList=admissionEntryList.stream().sorted(Comparator.comparing(AdmissionEntry::getMerit).reversed()).collect(Collectors.toList());

		for (AdmissionEntry admissionEntry : admissionEntryList) {

			if (!(admissionEntry.getRegistrationDate().isBefore(fromDate))
					&& !(admissionEntry.getRegistrationDate().isAfter(toDate))
					&& admissionEntry.getStatusSelect() == 2) {

				if (admissionEntry.getCollegeList() != null && !admissionEntry.getCollegeList().isEmpty()) {

					List<CollegeEntry> collegeEntryList = admissionEntry.getCollegeList();

					collegeEntryList = collegeEntryList.stream().sorted(Comparator.comparing(CollegeEntry::getSequence))
							.collect(Collectors.toList());

					System.err.print("name " + admissionEntry.getCandidate().getFullName() + " "
							+ admissionEntry.getFaculty().getName() + " " + admissionEntry.getMerit() + "\n");

					for (CollegeEntry collegeEntry : collegeEntryList) {

						System.err
								.print(collegeEntry.getCollege().getName() + "  " + collegeEntry.getSequence() + "\n");

						FacultyEntry selectedFacultyEntry = null;

						Boolean isCollegeSelected = false;
						if (collegeEntry.getCollege().getFacultyList() != null
								&& !collegeEntry.getCollege().getFacultyList().isEmpty()) {

							List<FacultyEntry> facultyEntryList = collegeEntry.getCollege().getFacultyList();

							for (FacultyEntry facultyEntry : facultyEntryList) {

								if (facultyEntry.getFaculty().equals(admissionEntry.getFaculty())
										&& facultyEntry.getSeats() > 0) {
									selectedFacultyEntry = facultyEntry;
									isCollegeSelected = true;
									break;
								}
							}
						}
						if (isCollegeSelected) {
							selectedFacultyEntry.setSeats(selectedFacultyEntry.getSeats() - 1);
							admissionEntry.setCollegeSelected(collegeEntry.getCollege());
							admissionEntry.setStatusSelect(admissionEntryRepo.ADMISSION_ENTRY_STATUS_ADMITTED);
							admissionEntry.setValidationDate(LocalDate.now());
							break;
						}
					}

					if (admissionEntry.getCollegeSelected() == null) {
						admissionEntry.setStatusSelect(admissionEntryRepo.ADMISSION_ENTRY_STATUS_CANCELLED);
					}
					admissionEntryRepo.persist(admissionEntry);
				}
			}
		}
	}

	public List<AdmissionEntry> sortAdmissonEntryInDecendingOrder(List<AdmissionEntry> admissionEntriesList) {
		Collections.sort(admissionEntriesList, new Comparator<AdmissionEntry>() {
			@Override
			public int compare(AdmissionEntry p1, AdmissionEntry p2) {
				BigDecimal p1Merit = p1.getMerit();
				BigDecimal p2Merit = p2.getMerit();
				if (p1Merit.compareTo(p2Merit) == 0) {
					return (p1.getRegistrationDate().compareTo(p2.getRegistrationDate()));
				} else {
					return p1Merit.compareTo(p2Merit);
				}
			}
		}.reversed());
		return admissionEntriesList;
	}
}
