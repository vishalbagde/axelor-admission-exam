package com.axelor.admission.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.axelor.admission.db.AdmissionEntry;
import com.axelor.admission.db.AdmissionProcess;
import com.axelor.admission.db.CollegeEntry;
import com.axelor.admission.db.FacultyEntry;
import com.axelor.admission.db.repo.AdmissionEntryRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class AdmissionProcessServiceImpl implements AdmissionProcessService {

	@Inject
	AdmissionEntryRepository admissionEntryRepo;

	@Transactional
	@Override
	public void computeAdmissionProcess(AdmissionProcess admissionProcess) {

		LocalDate fromDate = admissionProcess.getFromDate();
		LocalDate toDate = admissionProcess.getToDate();

		List<AdmissionEntry> admissionEntryList = admissionEntryRepo.all().fetch();

		// sort admission List base on merit
		admissionEntryList = sortAdmissonEntryInDecendingOrder(admissionEntryList);

		for (AdmissionEntry admissionEntry : admissionEntryList) {

			if (!(admissionEntry.getRegistrationDate().isBefore(fromDate))
					&& !(admissionEntry.getRegistrationDate().isAfter(toDate))
					&& admissionEntry.getStatusSelect() == 2) {

				if (admissionEntry.getCollegeList() != null && !admissionEntry.getCollegeList().isEmpty()) {

					// fetch and sort collegeEntryList base on sequence
					admissionEntry.setCollegeList(this.sortCollegeListInAsendingOrder(admissionEntry.getCollegeList()));

					System.err.print("name " + admissionEntry.getCandidate().getFullName() + " "
							+ admissionEntry.getFaculty().getName() + " " + admissionEntry.getMerit() + "\n");

					List<CollegeEntry> collegeEntryList = admissionEntry.getCollegeList();

					for (CollegeEntry collegeEntry : collegeEntryList) {

						System.err.print(
								"" + collegeEntry.getCollege().getName() + "  " + collegeEntry.getSequence() + "\n");

						FacultyEntry selectedFacultyEntry = null;

						List<FacultyEntry> facultyEntryList = collegeEntry.getCollege().getFacultyList();
						Boolean isCollegeSelected = false;
						for (FacultyEntry facultyEntry : facultyEntryList) {

							if (facultyEntry.getFaculty().equals(admissionEntry.getFaculty())
									&& facultyEntry.getSeats() > 0) {
								selectedFacultyEntry = facultyEntry;
								isCollegeSelected = true;
								break;

							}
						}

						if (isCollegeSelected) {
							selectedFacultyEntry.setSeats(selectedFacultyEntry.getSeats() - 1);
							admissionEntry.setCollegeSelected(collegeEntry.getCollege());
							admissionEntry.setStatusSelect(3);
							admissionEntry.setValidationDate(LocalDate.now());
							break;
						}

					}

					if (admissionEntry.getCollegeSelected() == null) {
						admissionEntry.setStatusSelect(4);
					}

					admissionEntryRepo.persist(admissionEntry);
				}
			}
		}
	}

	public List<CollegeEntry> sortCollegeListInAsendingOrder(List<CollegeEntry> collegeEntriesList) {
		Collections.sort(collegeEntriesList, new Comparator<CollegeEntry>() {
			@Override
			public int compare(CollegeEntry p1, CollegeEntry p2) {
				if (p1.getSequence() > p2.getSequence()) {
					return 1;
				} else {
					return -1;
				}
			}
		});

		return collegeEntriesList;
	}

	public List<AdmissionEntry> sortAdmissonEntryInDecendingOrder(List<AdmissionEntry> admissionEntriesList) {
		Collections.sort(admissionEntriesList, new Comparator<AdmissionEntry>() {
			@Override
			public int compare(AdmissionEntry p1, AdmissionEntry p2) {
				BigDecimal p1Merit = p1.getMerit();
				BigDecimal p2Merit = p2.getMerit();
				if (p1Merit.equals(p2Merit)) {

					return (p1.getRegistrationDate().compareTo(p2.getRegistrationDate()));

				} else if (p1Merit.compareTo(p2Merit) < 0) {

					return 1;
				} else {

					return -1;
				}
			}
		});

		return admissionEntriesList;
	}

}