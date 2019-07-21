package org.dlsu.arrowsmith.servlets.ASSYSTX2;

/*
 *  ASSYSTX2
 *  MANAGE PROFILES CONTROLLER
 *
 *  Contents:
 *  URL Mappings for the course annd
 *  faculty profiles of the ASSYSTX system.
 */

import org.dlsu.arrowsmith.classes.main.Response;
import org.dlsu.arrowsmith.classes.dtos.ASSYSTX2.ManageCourseDTO;
import org.dlsu.arrowsmith.classes.dtos.ASSYSTX2.ManageFacultyDTO;
import org.dlsu.arrowsmith.classes.dtos.ASSYSTX2.ManageFacultyLoadListDTO;
import org.dlsu.arrowsmith.classes.main.*;
import org.dlsu.arrowsmith.services.FacultyService;
import org.dlsu.arrowsmith.services.OfferingService;
import org.dlsu.arrowsmith.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Iterator;

@RestController
@RequestMapping({"/", "/courses", "/faculty"})
public class ProfilesController
{
    /*** Services ***/
    @Autowired
    private OfferingService offeringService;

    @Autowired
    private UserService userService;

    @Autowired
    private FacultyService facultyService;

    @Autowired
    private MessageSource messages;

    /*
     *  COURSE PROFILES
     *  URL MAPPING
     *
     */

    /* Retrieve all courses depending on constraints */
    @PostMapping(value = "/retrieve-course-list")
    public Response retrieveCourseList(@RequestBody String deptCode)
    {
        deptCode = deptCode.substring(0, deptCode.length() - 1);

        /* Initialize iterator */
        Iterator allCourses;

        if(deptCode.equalsIgnoreCase("ALL"))
            allCourses = offeringService.retrieveAllCourses();
        else
        {
            /* Retrieve Department */
            Department department = facultyService.retrieveDepartmentByDeptCode(deptCode);

            System.out.println("Department: " + department.getDeptCode());

            allCourses = offeringService.retrieveAllCoursesByDepartment(department);
        }

        /* Convert into DTO */
        ArrayList<ManageCourseDTO> dtos = new ArrayList<>();
        while(allCourses.hasNext())
        {
            /* Retrieve current iterated course */
            Course course = (Course) allCourses.next();

            /* Transfer into DTO */
            ManageCourseDTO dto = new ManageCourseDTO();
            dto.setCourseCode(course.getCourseCode());
            dto.setCourseName(course.getCourseName());
            dto.setCourseDesc(course.getCourseDesc());
            dto.setCourseUnits(course.getUnits());

            dtos.add(dto);
        }

        /* Check if empty */
        if(dtos.size() == 0)
            return new Response("Empty", null);

        return new Response("Done", dtos.iterator());
    }

    /* Create a new course profile */
    @PostMapping(value = "/create-new-course-profile")
    public Response createNewCourseProfile(@RequestBody ManageCourseDTO dto)
    {
        /* Initialize Course */
        Course newCourse = new Course();
        newCourse.setCourseCode(dto.getCourseCode());
        newCourse.setCourseName(dto.getCourseName());
        newCourse.setUnits(dto.getCourseUnits());

        if (dto.getCourseDesc().equalsIgnoreCase(""))
            newCourse.setCourseDesc("There is no description available for this course.");
        else
            newCourse.setCourseDesc(dto.getCourseDesc());

        /* Put into respective department and college */
        newCourse.setDepartment(userService.retrieveUser().getDepartment());
        newCourse.setCollege(userService.retrieveUser().getCollege());

        /* Save into database */
        offeringService.saveCourse(newCourse);

        /* Return a response */
        return new Response("Done", messages.getMessage("message.createCourseProfile", null, null));
    }

    /*
     *  FACULTY PROFILES
     *  URL MAPPING
     *
     */

    /* Retrieve all faculty based on department or other constraints */
    @GetMapping(value = "/retrieve-faculty-list")
    public Response retrieveFacultyList()
    {
        /* Initialize iterator */
        Iterator allFaculty = userService.retrieveAllFaculty();

        /* Check the constraint
        if(facultyType.equalsIgnoreCase("ALL"))
            allFaculty = userService.retrieveAllFaculty();
        else if(facultyType.equalsIgnoreCase("ACTIVE"))
            allFaculty = userService.retrieveAllActiveFaculty();
        else if(facultyType.equalsIgnoreCase("LEAVE"))
        {
            /* Retrieve all faculty load
            Iterator allFacultyLoad = facultyService.retrieveAllFacultyLoadByTerm(userService.retrieveCurrentTerm());

            ArrayList<User> onLeaveFaculty = new ArrayList<>();
            while(allFacultyLoad.hasNext())
            {
                FacultyLoad currLoad = (FacultyLoad) allFacultyLoad.next();
                if (currLoad.isOnLeave())
                    onLeaveFaculty.add(currLoad.getFaculty());
            }

            allFaculty = onLeaveFaculty.iterator();
        }
        else if(facultyType.equalsIgnoreCase("INACTIVE"))
            allFaculty = userService.retrieveAllInactiveFaculty();*/

        /* Retrieve the faculty */
        ArrayList<ManageFacultyDTO> dtos = new ArrayList<>();
        while(allFaculty.hasNext())
        {
            User faculty = (User) allFaculty.next();

            ManageFacultyDTO dto = new ManageFacultyDTO();
            dto.setFacultyName(faculty.getLastName() + ", " + faculty.getFirstName());
            dto.setFacultyType(faculty.getUserPosition());
            dto.setDepartment(faculty.getDepartment().getDeptName());

            /* Set Faculty Status and Total Units */
            FacultyLoad load = facultyService.retrieveFacultyLoadByFaculty(userService.retrieveCurrentTerm(), faculty);
            if(faculty.isActive() && load.isOnLeave())
                dto.setActive("On Leave");
            else if(faculty.isActive() && !load.isOnLeave())
                dto.setActive("Active");
            else if(!faculty.isActive())
                dto.setActive("Inactive");

            if(load == null)
                dto.setTotalUnits(0);
            else
                dto.setTotalUnits(load.getTotalLoad());

            dtos.add(dto);
        }

        if(dtos.size() == 0)
            return new Response("Empty", null);

        return new Response("Done", dtos.iterator());
    }

    /* Retrieve specific faculty profile */
    @PostMapping(value = "/retrieve-specific-faculty-profile")
    public Response retrieveSpecificFacultyList(@RequestBody String facultyName)
    {
        /* Retrieve Selected Faculty */
        User selectedFaculty = userService.findUserByFirstNameLastName(facultyName);

        /* Retrieve Faculty Load */
        FacultyLoad facultyLoad = facultyService.retrieveFacultyLoadByFaculty(userService.retrieveCurrentTerm(), selectedFaculty);

        /* Transfer to DTO */
        ManageFacultyDTO dto = new ManageFacultyDTO();
        dto.setFacultyName(facultyName);
        dto.setFacultyType(selectedFaculty.getUserPosition());
        dto.setDepartment(selectedFaculty.getDepartment().getDeptCode());

        /* Active Status */
        if(selectedFaculty.isActive() && !facultyLoad.isOnLeave())
            dto.setActive("Yes");
        else if(selectedFaculty.isActive() && facultyLoad.isOnLeave())
            dto.setActive("On Leave");
        else if(!selectedFaculty.isActive())
            dto.setActive("Inactive");

        /* Only Active Faculty have Faculty Load */
        if(selectedFaculty.isActive())
        {
            /* Numerical Units */
            dto.setTeachingUnits(facultyLoad.getTeachingLoad());
            dto.setResearchUnits(facultyLoad.getResearchLoad());
            dto.setAdminUnits(facultyLoad.getAdminLoad());

            /* Teaching Load */
            ArrayList<ManageFacultyLoadListDTO> teachingLoads = new ArrayList<>();
            Iterator offerings = offeringService.retrieveAllOfferingsByFaculty(selectedFaculty, userService.retrieveCurrentTerm());
            while(offerings.hasNext())
            {
                CourseOffering offering = (CourseOffering) offerings.next();

                /* Create load instance */
                ManageFacultyLoadListDTO load = new ManageFacultyLoadListDTO();
                load.setLoadName(offering.getCourse().getCourseCode());
                load.setLoadUnits(3.0);
                teachingLoads.add(load);
            }

            /* Admin and Research Load */
            ArrayList<ManageFacultyLoadListDTO> adminLoads = new ArrayList<>();
            ArrayList<ManageFacultyLoadListDTO> researchLoads = new ArrayList<>();
            Iterator deloadings = facultyService.retrieveAllDeloadInstanceByFaculty(userService.retrieveCurrentTerm(), selectedFaculty);
            while(deloadings.hasNext())
            {
                DeloadInstance deloading = (DeloadInstance) deloadings.next();

                /* Create load instance */
                ManageFacultyLoadListDTO load = new ManageFacultyLoadListDTO();
                load.setLoadName(deloading.getDeloading().getDeloadCode());
                load.setLoadUnits(deloading.getDeloading().getUnits());

                /* Determine where to add: research or admin */
                if(deloading.getDeloading().getDeloadType().equals("RL"))
                    researchLoads.add(load);
                else if(deloading.getDeloading().getDeloadType().equals("AL"))
                    adminLoads.add(load);
            }

            dto.setTeachingLoad(teachingLoads.iterator());
            dto.setAdminLoad(adminLoads.iterator());
            dto.setResearchLoad(researchLoads.iterator());
        }

        return new Response("Done", dto);
    }

    /* Create a new faculty profile */
    @PostMapping(value = "/create-new-faculty-profile")
    public Response createNewFacultyProfile(@RequestBody ManageFacultyDTO dto)
    {
        /* Split names */
        String[] names = dto.getFacultyName().split(", ");

        /* TODO: Initialize new user/faculty */
        User newUser = new User();
        newUser.setFirstName(names[1]);
        newUser.setLastName(names[0]);

        return new Response();
    }
}
