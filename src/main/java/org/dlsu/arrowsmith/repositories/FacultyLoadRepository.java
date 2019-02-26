package org.dlsu.arrowsmith.repositories;

import org.dlsu.arrowsmith.classes.College;
import org.dlsu.arrowsmith.classes.Department;
import org.dlsu.arrowsmith.classes.FacultyLoad;
import org.dlsu.arrowsmith.classes.User;
import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;

public interface FacultyLoadRepository extends CrudRepository<FacultyLoad, Long> {
    ArrayList<FacultyLoad> findAllByStartAYAndEndAYAndTerm(int startAY, int endAY, int term);
    FacultyLoad findFacultyLoadByStartAYAndEndAYAndTermAndFaculty(int startAY, int endAY, int term, User faculty);
    ArrayList<FacultyLoad> findAllByStartAYAndEndAYAndTermAndDepartment(int startAY, int endAY, int term, Department department);
    ArrayList<FacultyLoad> findAllByStartAYAndEndAYAndTermAndCollege(int startAY, int endAY, int term, College college);
    ArrayList<FacultyLoad> findAllByFaculty(User faculty);
    FacultyLoad findFacultyLoadByLoadId(Long loadId);
}
