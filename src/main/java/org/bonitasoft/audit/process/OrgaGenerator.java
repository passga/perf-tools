package org.bonitasoft.audit.process;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.bonitasoft.deployer.Deployer;
import com.devskiller.jfairy.Fairy;
import org.bonitasoft.audit.process.xml.ExportedGroup;
import org.bonitasoft.audit.process.xml.ExportedRole;
import org.bonitasoft.audit.process.xml.ExportedUser;
import org.bonitasoft.audit.process.xml.ExportedUserMembership;
import org.bonitasoft.audit.process.xml.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrgaGenerator {
    private static final Logger log = LoggerFactory.getLogger(OrgaGenerator.class);

    private static Organization organization = null;
    private static OrgaGenerator orgaGenerator = null;
    private static Map<String,ExportedUser> managerByUserName = null;
    private static JAXBContext jc = null;
    private static Marshaller marshaller = null;
    private static TreeMap<String,String> managerIdByUSerName;

    private OrgaGenerator() throws Exception {
        organization = new Organization();
        jc = JAXBContext.newInstance(Organization.class);
        managerByUserName=new HashMap<>();
        marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    }

    public static OrgaGenerator getInstance() throws Exception {
        if (orgaGenerator == null) {
            orgaGenerator = new OrgaGenerator();
        }
        return orgaGenerator;
    }

    public enum Role {
        MEMBER, RESP, EMPLOYEE,ADMIN
    }


    public enum LevelGroup {
        COMPANY, COUNTRY, TEAM, CITY;
    }

    public enum Group {
        BONITA(null, "Bonita", LevelGroup.COMPANY),  FRANCE("/Bonita", "France", LevelGroup.COUNTRY), US("/Bonita", "US", LevelGroup.COUNTRY), GRENOBLE("/Bonita/France", "Grenoble", LevelGroup.CITY), SF("/Bonita/US", "San Francisco", LevelGroup.CITY), RD("/Bonita", "R&D", LevelGroup.TEAM), SUPPORT("/Bonita", "Support", LevelGroup.TEAM), HR("/Bonita", "HR", LevelGroup.TEAM);

        private String parentPath;
        private String displayName;
        private LevelGroup level;


        Group(String parentPath, String displayName, LevelGroup levelGroup) {
            this.parentPath = parentPath;
            this.displayName = displayName;
            this.level = levelGroup;
        }

        public String getParentPath() {
            return parentPath;
        }

        public String getDisplayName() {
            return displayName;
        }

        public LevelGroup getLevel() {
            return level;
        }


    }

    public static Organization getOrganization() {
        return organization;
    }

    public  OrgaGenerator buildOrganization(int numberUserByTeamGroup) {
        log.info("Generating organization xml file");
        Arrays.asList(Role.values()).stream().forEach(role -> {
            organization.getRoles().add(createRole(role.name()));

        });

        ExportedUser theBoss = buildUser(null);
        theBoss.setUserName("walter.bates");
        theBoss.setFirstName("walter");
        theBoss.setLastName("bates");
        organization.getUsers().add(theBoss);
        managerByUserName.put(theBoss.getUserName(),theBoss);
        organization.getMemberships().add(addMemberShip(theBoss.getUserName(), Group.BONITA.getDisplayName(), Group.BONITA.getParentPath(), Role.EMPLOYEE.name()));
        organization.getMemberships().add(addMemberShip(theBoss.getUserName(), Group.BONITA.getDisplayName(), Group.BONITA.getParentPath(), Role.ADMIN.name()));

        for (Group group : Arrays.asList(Group.values())) {
            organization.getGroups().add(createGroup(group.getDisplayName(), group.getParentPath()));
            if (group.getLevel() == LevelGroup.TEAM) {
                buildTeamUsersWithMemberShip(numberUserByTeamGroup, group, theBoss.getUserName());
            }
        }

        return orgaGenerator;
    }


    private  void buildTeamUsersWithMemberShip(int number, Group group, String bossUserName) {
        ExportedUser resp = buildUser(bossUserName);
        organization.getUsers().add(resp);
        managerByUserName.put(resp.getUserName(),resp);
        organization.getMemberships().add(addMemberShip(resp.getUserName(), group.getDisplayName(), group.getParentPath(), Role.RESP.name()));
        organization.getMemberships().add(addMemberShip(resp.getUserName(), Group.BONITA.getDisplayName(), Group.BONITA.getParentPath(), Role.EMPLOYEE.name()));

        for (int j = 0; j < number; j++) {

            ExportedUser member = buildUser(resp.getUserName());
            organization.getUsers().add(member);
            if ((j % 2) == 0) {
                organization.getMemberships().add(addMemberShip(member.getUserName(), Group.GRENOBLE.getDisplayName(), Group.GRENOBLE.getParentPath(), Role.MEMBER.name()));
            } else {
                organization.getMemberships().add(addMemberShip(member.getUserName(), Group.SF.getDisplayName(), Group.SF.getParentPath(), Role.MEMBER.name()));
            }

            organization.getMemberships().add(addMemberShip(member.getUserName(), group.getDisplayName(), group.getParentPath(), Role.MEMBER.name()));
            organization.getMemberships().add(addMemberShip(member.getUserName(), Group.BONITA.getDisplayName(), Group.BONITA.getParentPath(), Role.EMPLOYEE.name()));
        }


    }

    private  ExportedUserMembership addMemberShip(String userName, String groupName, String groupParentPath, String roleName) {
        ExportedUserMembership exportedUserMembership = new ExportedUserMembership();
        exportedUserMembership.setGroupName(groupName);
        exportedUserMembership.setGroupParentPath(groupParentPath);
        exportedUserMembership.setRoleName(roleName);
        exportedUserMembership.setUserName(userName);
        return exportedUserMembership;
    }

    private  ExportedUser buildUser(String managerUserName) {
        Fairy fairy = Fairy.create();
        ExportedUser exportedUser = new ExportedUser();
        exportedUser.setFirstName(fairy.person().getFirstName());
        exportedUser.setLastName(fairy.person().getLastName());
        exportedUser.setPassword("bpm");
        exportedUser.setProfessionalAddress(fairy.person().getEmail());
        int i = organization.getUsers().size() + 1;
        exportedUser.setUserName("userName"+ i) ;
        exportedUser.setManagerUserName(managerUserName);
        return exportedUser;
    }


    private  ExportedRole createRole(String name) {
        ExportedRole exportedRole = new ExportedRole();
        exportedRole.setDisplayName(name);
        exportedRole.setName(name);
        exportedRole.setDescription(name);
        exportedRole.setIconPath("");
        return exportedRole;
    }

    private  ExportedGroup createGroup(String name, String parentPath) {
        ExportedGroup exportedGroup = new ExportedGroup();
        exportedGroup.setDisplayName(name);
        exportedGroup.setName(name);
        exportedGroup.setDescription(name);
        exportedGroup.setParentPath(parentPath);
        return exportedGroup;
    }

    public ExportedGroup getRandomGroup() {
        Random random = new Random();
        return organization.getGroups().get(random.nextInt(organization.getGroups().size()));
    }

    public ExportedRole getRandomRole() {
        Random random = new Random();
        return organization.getRoles().get(random.nextInt(organization.getRoles().size()));
    }

    public ExportedUser getRandomUser() {
        Random random = new Random();
        return organization.getUsers().get(random.nextInt(organization.getUsers().size()));
    }

    public  void export(File file) throws Exception {
        File orgaFile = new File(file, "orga.xml");
        marshaller.marshal(organization, orgaFile);
        log.info("Organization was exported to xml file {} ", orgaFile);
    }

}
