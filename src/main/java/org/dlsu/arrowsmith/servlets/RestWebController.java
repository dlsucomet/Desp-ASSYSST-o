package org.dlsu.arrowsmith.servlets;

import org.dlsu.arrowsmith.classes.dro.Response;
import org.dlsu.arrowsmith.classes.dtos.*;
import org.dlsu.arrowsmith.classes.main.*;
import org.dlsu.arrowsmith.services.FacultyService;
import org.dlsu.arrowsmith.services.OfferingService;
import org.dlsu.arrowsmith.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Filter;

@RestController
@RequestMapping({"/apo", "/cvc"})
public class RestWebController {
    /*** Services ***/
    @Autowired
    private OfferingService offeringService;

    @Autowired
    private UserService userService;

    @Autowired
    private FacultyService facultyService;

    /***
     *
     *  ACTIVE
     *  URL MAPPING
     *
     */
    @PostMapping(value = "/search-course")
    public Response filterCoursesbyCourseCode(@RequestBody String courseCode, Model model) {
        ArrayList<CourseOffering> searchCourses = offeringService.retrieveCourseOfferingSearch(courseCode);

        ArrayList<OfferingModifyDto> listOfferDtos = convertToDTO(searchCourses.iterator());

        Response response = new Response();
        if (listOfferDtos.size() == 0)
            response.setStatus("Error");
        else
            response.setStatus("Done");

        response.setData(listOfferDtos);
        return response;
    }

    @PostMapping(value = "/filter-days")
    public Response filterCoursesOnDay(@RequestBody String day, Model model) {
        char charDay = day.charAt(1);
        ArrayList<CourseOffering> dayCourses = offeringService.getAllCoursesOnDay(charDay);

        ArrayList<OfferingModifyDto> listOfferDtos = convertToDTO(dayCourses.iterator());

        /* Create Response Object */
        Response response = new Response();
        if (listOfferDtos.size() == 0)
            response.setStatus("Error");
        else
            response.setStatus("Done");

        response.setData(listOfferDtos);
        return response;
    }

    //General function for filtering
    @PostMapping(value = "/left-filter")
    public Response filterCourseOfferings(@RequestBody FilterDto filterValues, Model model) {
        ArrayList<ArrayList<CourseOffering>> filterHolder = new ArrayList<ArrayList<CourseOffering>>();
        //THIS IS WHERE INTERSECTION WILL HAPPEN BITCHES
        ArrayList<OfferingModifyDto> listOfferDtos = new ArrayList<>();
        ArrayList<CourseOffering> termFilter = new ArrayList<>();
        ArrayList<CourseOffering> classTypeFilter = new ArrayList<>();
        ArrayList<CourseOffering> roomTypeFilter = new ArrayList<>();
        ArrayList<CourseOffering> timeBlockFilter = new ArrayList<>();
        ArrayList<CourseOffering> holder = new ArrayList<>();
        if (filterValues.getTerm().equals("All") && filterValues.getClassType().equals("All") && filterValues.getRoomType().equals("All") &&
                filterValues.getTimeBlock().equals("All"))
            holder = offeringService.generateSortedArrayListCourseOfferings(2016, 2017, 1);
        else {
            //Get all results
            if (!filterValues.getTerm().equals("All"))
                termFilter = offeringService.retrieveCourseOfferingsTerm(Integer.parseInt(filterValues.getTerm()));
            if (!filterValues.getClassType().equals("All"))
                classTypeFilter = offeringService.retrieveCourseOfferingsClassType(filterValues.getClassType());
            if (!filterValues.getRoomType().equals("All"))
                roomTypeFilter = offeringService.retrieveCourseOfferingsRoomType(filterValues.getRoomType());
            if (!filterValues.getTimeBlock().equals("All"))
                timeBlockFilter = offeringService.retrieveCourseOfferingsTimeslot(filterValues.getTimeBlock());
            holder = offeringService.generateSortedArrayListCourseOfferings(2016, 2017, 1);
            //Intersection time
            //Get all arraylists that are greater than 0
            if (!filterValues.getTerm().equals("All"))
                filterHolder.add(termFilter);
            if (!filterValues.getClassType().equals("All"))
                filterHolder.add(classTypeFilter);
            if (!filterValues.getRoomType().equals("All"))
                filterHolder.add(roomTypeFilter);
            if (!filterValues.getTimeBlock().equals("All"))
                filterHolder.add(timeBlockFilter);

            //Intersect all of them
            for (int i = 0; i < filterHolder.size(); i++)
                holder = offeringService.generateIntersectionLists(holder, filterHolder.get(i));
        }

        offeringService.setFilteredCourses(holder);
        for (CourseOffering cs : holder) {
            OfferingModifyDto currDTO;
            currDTO = transferToDTO(cs);
            listOfferDtos.add(currDTO);
        }

        /* Create Response Object */
        Response response = new Response();
        if (listOfferDtos.size() == 0)
            response.setStatus("Error");
        else
            response.setStatus("Done");

        response.setData(listOfferDtos);
        return response;
    }


    /* Retrieve All Course Offerings through GET */
    @GetMapping(value = "/show-offerings")
    public Response showAllOfferings(Model model) {
        /* Create new list for course offerings */
        Iterator allOfferings = offeringService.generateSortedCourseOfferings(2016, 2017, 1);

        /* Convert to DTO */
        ArrayList<OfferingModifyDto> listOfferDtos = new ArrayList<OfferingModifyDto>();
        while (allOfferings.hasNext()) {
            CourseOffering offering = (CourseOffering) allOfferings.next();

            /* Transfer to DTO */
            OfferingModifyDto currDTO = transferToDTO(offering);

            listOfferDtos.add(currDTO);
        }

        /* Create Response Object */
        Response response = new Response();
        response.setStatus("Done");
        response.setData(listOfferDtos);
        model.addAttribute("allOfferings", listOfferDtos.iterator());

        return response;
    }

    /* Retrieve All Filtered Course Offerings through GET */
    @GetMapping(value = "/get-filtered-offerings")
    public Response retrieveFilteredOfferings(Model model) {
        /* Create new list for course offerings */
        Iterator allOfferings = offeringService.getFilteredCourses().iterator();

        /* Convert to DTO */
        ArrayList<OfferingModifyDto> listOfferDtos = new ArrayList<OfferingModifyDto>();
        while (allOfferings.hasNext()) {
            CourseOffering offering = (CourseOffering) allOfferings.next();

            /* Transfer to DTO */
            OfferingModifyDto currDTO = transferToDTO(offering);

            listOfferDtos.add(currDTO);
        }

        /* Create Response Object */
        Response response = new Response();
        response.setStatus("Done");
        response.setData(listOfferDtos);
        model.addAttribute("allOfferings", listOfferDtos.iterator());

        return response;
    }

    /* Retrieve All Filtered Course Offerings through GET */
    @GetMapping(value = "/get-filtered-day")
    public Response retrieveDayFilteredOfferings(Model model) {
        /* Create new list for course offerings */
        Iterator allOfferings = offeringService.getDayFilteredCourses().iterator();

        /* Convert to DTO */
        ArrayList<OfferingModifyDto> listOfferDtos = new ArrayList<OfferingModifyDto>();
        while (allOfferings.hasNext()) {
            CourseOffering offering = (CourseOffering) allOfferings.next();

            /* Transfer to DTO */
            OfferingModifyDto currDTO = transferToDTO(offering);

            listOfferDtos.add(currDTO);
        }

        /* Create Response Object */
        Response response = new Response();
        response.setStatus("Done");
        response.setData(listOfferDtos);
        model.addAttribute("allOfferings", listOfferDtos.iterator());

        return response;
    }

    /* Retrieve All Filtered Course Offerings through GET */
    @GetMapping(value = "/get-filtered-search")
    public Response retrieveSearchOfferings(Model model) {
        /* Create new list for course offerings */
        Iterator allOfferings = offeringService.getSearchCourses().iterator();

        /* Convert to DTO */
        ArrayList<OfferingModifyDto> listOfferDtos = new ArrayList<OfferingModifyDto>();
        while (allOfferings.hasNext()) {
            CourseOffering offering = (CourseOffering) allOfferings.next();

            /* Transfer to DTO */
            OfferingModifyDto currDTO = transferToDTO(offering);

            listOfferDtos.add(currDTO);
        }

        /* Create Response Object */
        Response response = new Response();
        response.setStatus("Done");
        response.setData(listOfferDtos);
        model.addAttribute("allOfferings", listOfferDtos.iterator());

        return response;
    }

    /* Modify Course Offering through POST */
    @PostMapping(value = "/modify-offering")
    public Response modifyCourseOffering(@RequestBody OfferingModifyDto offering) {

        /* Retrieve Offering from Database */
        CourseOffering currOffering = offeringService.retrieveCourseOffering(offering.getOfferingId());

        /* Course Offering Section */
        if (!currOffering.getSection().equals(offering.getClassSection()))
            currOffering.setSection(offering.getClassSection());

        /* Course Offering Type */
        if (!currOffering.getStatus().equals(offering.getClassStatus()))
            currOffering.setStatus(offering.getClassStatus());

        /* Find Room Object */
        Room newRoom = offeringService.retrieveRoomByRoomCode(offering.getRoomCode());

        /* Update Days Object */
        Set<Days> daysSet = currOffering.getDaysSet();

        boolean noConflicts = true;
        if (daysSet == null)        /* No current class days and room for the offering */
        {
            // If Input Day 1 is not null or "-" in the form
            if (!(offering.getDay1() == '-') && noConflicts)
            {
                /* Create a new Days object */
                Days newDay1 = createNewDay(offering, newRoom, currOffering, 1);
                daysSet.add(newDay1);
                offeringService.saveDays(newDay1);
            }

            // If Day 2 is not null or "-" in the form
            if (!(offering.getDay2() == '-'))
            {
                /* Create a new Days object */
                Days newDay2 = createNewDay(offering, newRoom, currOffering, 2);
                daysSet.add(newDay2);
                offeringService.saveDays(newDay2);
            }
        }
        else                        /* There is already an assigned days for the offering */
        {
            boolean isDay1Done = false;
            for (Days dayInstance : daysSet)
            {
                // If input Day 1 is not null or "-" in the form - update day instance
                if (!(offering.getDay1() == '-') && offering.getDay1() != dayInstance.getclassDay() && !isDay1Done)
                {
                    Long daysId = dayInstance.getdaysId();
                    dayInstance = createNewDay(offering, newRoom, currOffering, 1);
                    dayInstance.setdaysId(daysId);
                    isDay1Done = true;
                    continue;
                }
                // If input Day 1 is null or "-" in the form - delete day instance
                else if (offering.getDay1() == '-' && !isDay1Done)
                {
                    daysSet.remove(dayInstance);
                    offeringService.deleteSpecificDay(dayInstance);
                    isDay1Done = true;
                    continue;
                }

                // If Day 2 is not null or "-" in the form
                if (!(offering.getDay2() == '-') && offering.getDay2() != dayInstance.getclassDay() && isDay1Done)
                {
                    Long daysId = dayInstance.getdaysId();
                    dayInstance = createNewDay(offering, newRoom, currOffering, 2);
                    dayInstance.setdaysId(daysId);
                }
                // If Day 2 is null or "-" in the form
                else if (offering.getDay2() == '-' && isDay1Done)
                {
                    daysSet.remove(dayInstance);
                    offeringService.deleteSpecificDay(dayInstance);
                }
            }
        }

        /* Faculty */
        User currFaculty = currOffering.getFaculty();
        User newFaculty = userService.findUserByFirstNameLastName(offering.getFaculty());

        /* If there is a current faculty and is being replaced by a new faculty */
        if (currFaculty.getUserId() != null && currFaculty.getUserId() != newFaculty.getUserId())
        {
            /* Reduce Faculty Load of to be replaced Faculty */
            double newTLUnits = -1.0 * currOffering.getCourse().getUnits();
            modifyFacultyLoad(currFaculty, currOffering, newTLUnits);

            /* Assign faculty to Course Offering */
            currOffering.setFaculty(newFaculty);

            /* Add Faculty Load to newly assigned Faculty */
            modifyFacultyLoad(newFaculty, currOffering, currOffering.getCourse().getUnits());
        }
        /* No faculty assigned yet to the offering */
        else if (currFaculty.getUserId() == null && newFaculty != null)
        {
            /* Assign faculty to Course Offering */
            currOffering.setFaculty(newFaculty);

            /* Add Faculty Load to newly assigned Faculty */
            modifyFacultyLoad(newFaculty, currOffering, currOffering.getCourse().getUnits());
        }

        /* Save Course Offering to Database */
        offeringService.saveCourseOffering(currOffering);

        /* Create Response Object */
        Response response = new Response();
        response.setStatus("Done");
        response.setData(offering);

        return response;
    }

    /* Retrieve Specific Course Offering through POST */
    @PostMapping(value = "/find-offering")
    public Response findCourseOffering(@RequestBody Long offeringId) {
        /* Retrieve specific course offering from database */
        //CourseOffering selectedOffering = offeringService.retrieveCourseOffering(Long.parseLong(offeringId));
        CourseOffering selectedOffering = offeringService.retrieveCourseOffering(offeringId);

        /* Transfer to DTO for easier processing for front-end */
        OfferingModifyDto offeringDto = transferToDTO(selectedOffering);

        //System.out.println(offeringDto.getStartTime() + offeringDto.getEndTime());
        /* Create new Response object */
        Response response = new Response();
        response.setStatus("Done");
        response.setData(offeringDto);

        return response;
    }

    /* Find all rooms that are available at this time and day */
    @PostMapping(value = "/check-rooms")
    public Response showRoomsApplicable(@RequestBody ModifyRoomDto timeInfo)
    {
        //Get all characters
        char day1 = timeInfo.getDay1();
        char day2 = timeInfo.getDay2();
        String startTime = timeInfo.getStartTime();
        String endTime = timeInfo.getEndTime();

        ArrayList<Room> roomList = offeringService.roomRuleChecking(day1, day2, startTime, endTime);
        ArrayList<RoomDto> transferableroomList = new ArrayList<>();

        System.out.println(roomList.size());
        for(Room r: roomList)
        {
            RoomDto roomDto = new RoomDto();
            roomDto.setRoomCode(r.getRoomCode());
            roomDto.setRoomType(r.getRoomType());
            roomDto.setCapacity(r.getRoomCapacity());
            roomDto.setBuilding(r.getBuilding().getBldgName());
            transferableroomList.add(roomDto);
        }
        /* Create Response Object */
        Response response = new Response();
        if(roomList.size() <= 0)
            response.setStatus("Error");
        else
            response.setStatus("Done");

        response.setData(transferableroomList);
        return response;
    }

    /* Find all faculty that are available at this timeslot AND are applicable for teaching*/
    @PostMapping(value = "/check-faculty")
    public Response showFacultyApplicable(@RequestBody ModifyRoomDto timeInfo)
    {
        //Get all data to be used for evaluation
        char day1 = timeInfo.getDay1();
        char day2 = timeInfo.getDay2();
        String startTime = timeInfo.getStartTime();
        String endTime = timeInfo.getEndTime();

        ArrayList<FacultyLoad> facultyList = facultyService.facultyRuleChecking(day1, day2, startTime, endTime);
        ArrayList<FacultyLoadDto> transferableFacultyList = new ArrayList<>();

        for(FacultyLoad fl: facultyList)
        {
            FacultyLoadDto currLoad = new FacultyLoadDto();
            currLoad.setAdminLoad(fl.getAdminLoad());
            currLoad.setResearchLoad(fl.getResearchLoad());
            currLoad.setTeachingLoad(fl.getTeachingLoad());
            currLoad.setTotalLoad(fl.getTotalLoad());
            currLoad.setFirstName(fl.getFaculty().getFirstName());
            currLoad.setLastName(fl.getFaculty().getLastName());
            transferableFacultyList.add(currLoad);
        }

        /* Create Response Object*/
        Response response = new Response();
        if(facultyList.size() <= 0)
            response.setStatus("Error");
        else
            response.setStatus("Done");

        response.setData(transferableFacultyList);
        return response;
    }

    /* Retrieve All Concerns through GET */
    @GetMapping(value = "/get-concerns")
    public Response retrieveConcerns(@RequestBody String userID, Model model)
    {
        Long converUserID = Long.parseLong(userID);
        /* Create new list for concerns */
        Iterator allConcerns = userService.retrieveAllConcernsByReceiver(userService.findUserByIDNumber(converUserID));

        /* Convert to DTO */
        ArrayList<ConcernDto> listConcernDtos = new ArrayList<>();
        while(allConcerns.hasNext())
        {
            Concern concern = (Concern) allConcerns.next();

            /* Transfer to DTO */
            ConcernDto conDTO = transferToConcernDTO(concern);

            listConcernDtos.add(conDTO);
        }

        /* Create Response Object */
        Response response = new Response();
        response.setStatus("Done");
        response.setData(listConcernDtos);

        return response;
    }

    /* Send and Save a Concern using POST */
    @PostMapping(value = "/post-concerns")
    public Response retrieveConcerns(@RequestBody ConcernDto concernSend, Model model)
    {

        //convert concernDTO
        Concern concern = this.transferToConcern(concernSend);
        //save concern
        this.userService.saveConcern(concern);
        /* Create Response Object */
        Response response = new Response();
        response.setStatus("Done");
        return response;
    }

    /***
     **
     ** FUNCTIONS
     **
     */

    /* Function for converting a list of CourseOfferings to OfferingModifyDto */
    private ArrayList<OfferingModifyDto> convertToDTO(Iterator allOfferings) {
        /* Initialize arraylist */
        ArrayList<OfferingModifyDto> offerings = new ArrayList<>();

        while (allOfferings.hasNext()) {
            CourseOffering offering = (CourseOffering) allOfferings.next();

            /* Transfer to DTO */
            OfferingModifyDto currDTO = transferToDTO(offering);

            offerings.add(currDTO);
        }

        return offerings;
    }

    /* Function for converting a CourseOffering to OfferingModifyDto */
    private OfferingModifyDto transferToDTO(CourseOffering offering) {
        OfferingModifyDto modifyDto = new OfferingModifyDto();

        /* Offering ID */
        modifyDto.setOfferingId(offering.getofferingId());

        /* Course Code */
        modifyDto.setCourseCode(offering.getCourse().getCourseCode());

        /* Section */
        if (!offering.getSection().equals(""))
            modifyDto.setClassSection(offering.getSection());
        else
            modifyDto.setClassSection("None");

        /* Offering Status/Type */
        if (!offering.getStatus().equals(""))
            modifyDto.setClassStatus(offering.getStatus());
        else
            modifyDto.setClassStatus("Regular");

        /* Offering Faculty */
        if (offering.getFaculty() != null)
            modifyDto.setFaculty(offering.getFaculty().getLastName() + ", " + offering.getFaculty().getFirstName());
        else
            modifyDto.setFaculty("Unassigned");

        /* Days */
        boolean day1Done = false;
        for (Days day : offering.getDaysSet()) {
            /* Class Day */
            if (!day1Done)
                modifyDto.setDay1(day.getclassDay());
            else
                modifyDto.setDay2(day.getclassDay());

            /* Room */
            if (day.getRoom() != null)
                modifyDto.setRoomCode(day.getRoom().getRoomCode());
            else if (day.getRoom() == null || day.getRoom().getRoomId() == 11111111)
                modifyDto.setRoomCode("Unassigned");

            /* Timeslot */
            if (!day.getbeginTime().equals("") && !day.getendTime().equals("")) {
                if (day.getbeginTime().length() == 3) {
                    modifyDto.setStartTime("0" + day.getbeginTime().charAt(0) + ":" + day.getbeginTime().substring(1, 3));
                    //System.out.println(day.getbeginTime().charAt(0) + ":" + day.getbeginTime().substring(1, 3));
                } else if (day.getbeginTime().length() == 4) {
                    modifyDto.setStartTime(day.getbeginTime().substring(0, 2) + ":" + day.getbeginTime().substring(2, 4));
                    //System.out.println(day.getbeginTime().substring(0, 2) + ":" + day.getbeginTime().substring(2, 4));
                }
                if (day.getendTime().length() == 3) {
                    modifyDto.setEndTime("0" + day.getendTime().charAt(0) + ":" + day.getendTime().substring(1, 3));
                    //System.out.println(day.getendTime().charAt(0) + ":" + day.getendTime().substring(1, 3));
                } else if (day.getendTime().length() == 4) {
                    modifyDto.setEndTime(day.getendTime().substring(0, 2) + ":" + day.getendTime().substring(2, 4));
                    //System.out.println(day.getendTime().substring(0, 2) + ":" + day.getendTime().substring(2, 4));
                }
            } else {
                modifyDto.setStartTime("00:00");
                modifyDto.setEndTime("00:00");
            }
            day1Done = true;
        }
        if (modifyDto.getDay1() == '\0') {
            modifyDto.setDay1('-');
            modifyDto.setRoomCode("Unassigned");
            modifyDto.setStartTime("00:00");
            modifyDto.setEndTime("00:00");
            modifyDto.setDay2('-');
        }

        return modifyDto;
    }

    /* Function to generally create a new Days object */
    private Days createNewDay(OfferingModifyDto offering, Room newRoom, CourseOffering currOffering, int dayNumber) {
        Days newDay = new Days();

        System.out.println("Hello world i'm here in days");

        /* Letter Day */
        if (dayNumber == 1)
            newDay.setclassDay(offering.getDay1());
        else
            newDay.setclassDay(offering.getDay2());

        /* Start Time */
        newDay.setbeginTime(offering.getStartTime().replace(":", ""));

        /* End Time */
        newDay.setendTime(offering.getEndTime().replace(":", ""));

        /* Room */
        newDay.setRoom(newRoom);

        /* Course Offering */
        newDay.setCourseOffering(currOffering);

        return newDay;
    }

    /* Function to perform modifications on a faculty's faculty load */
    private void modifyFacultyLoad(User currFaculty, CourseOffering currOffering, double newTLUnits)
    {
        FacultyLoad currFacultyLoad = facultyService.retrieveFacultyLoadByFaculty(currOffering.getStartAY(), currOffering.getEndAY(),
                currOffering.getTerm(), currFaculty);
        currFacultyLoad.setTeachingLoad(currFacultyLoad.getTeachingLoad() + newTLUnits);
        facultyService.saveFacultyLoad(currFacultyLoad);
    }

    /* Function to transfer Concern object to Concern DTO */
    public ConcernDto transferToConcernDTO(Concern concern)
    {
        ConcernDto concernDto = new ConcernDto();

        concernDto.setConcernId(concern.getconcernId());
        concernDto.setMessage(concern.getMessage());
        concernDto.setUserId(concern.getReceiver().getUserId());
        concernDto.setSendUserId(concern.getSender().getUserId());
        concernDto.setSenderFirstName(concern.getSender().getFirstName());
        concernDto.setSenderLastName(concern.getSender().getLastName());

        return concernDto;
    }

    /* Function to transfer ConcernDto object to Concern */
    public Concern transferToConcern(ConcernDto concernDto)
    {
        Concern concern = new Concern();
        concern.setSender(userService.findUserByIDNumber(concernDto.getUserId()));
        concern.setReceiver(userService.findUserByIDNumber(concernDto.getSendUserId()));
        concern.setMessage(concernDto.getMessage());
        return concern;
    }
}