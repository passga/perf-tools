package org.bonitasoft.audit.process;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.devskiller.jfairy.Fairy;
import org.bonitasoft.audit.process.xml.ExportedGroup;
import org.bonitasoft.audit.process.xml.ExportedRole;
import org.bonitasoft.audit.process.xml.ExportedUser;
import org.bonitasoft.audit.process.xml.ExportedUserMembership;
import org.bonitasoft.audit.process.xml.Organization;
import org.bonitasoft.engine.api.APIClient;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.identity.ContactDataCreator;
import org.bonitasoft.engine.identity.GroupCreator;
import org.bonitasoft.engine.identity.RoleCreator;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserCreator;

public class OrgaGenerator {

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

    public static OrgaGenerator createInstance() throws Exception {
        if (orgaGenerator == null) {
            orgaGenerator = new OrgaGenerator();
        }
        return orgaGenerator;
    }

    public enum Role {
        MEMBER, RESP, EMPLOYEE
    }


    public enum LevelGroup {
        COMPANY, COUNTRY, TEAM, CITY;
    }

    public enum Group {
        BONITA(null, "Bonita", LevelGroup.COMPANY), FRANCE("/Bonita", "France", LevelGroup.COUNTRY), US("/Bonita", "US", LevelGroup.COUNTRY), GRENOBLE("/Bonita/France", "Grenoble", LevelGroup.CITY), SF("/Bonita/US", "San Francisco", LevelGroup.CITY), RD("/Bonita", "R&D", LevelGroup.TEAM), SUPPORT("/Bonita", "Support", LevelGroup.TEAM), HR("/Bonita", "HR", LevelGroup.TEAM);

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


    public  OrgaGenerator buildOrganization(int numberUserByTeamGroup) {
        Arrays.asList(Role.values()).stream().forEach(role -> {
            organization.getRoles().add(createRole(role.name()));

        });

        ExportedUser theBoss = buildUser(null);
        organization.getUsers().add(theBoss);
        managerByUserName.put(theBoss.getUserName(),theBoss);
        organization.getMemberships().add(addMemberShip(theBoss.getUserName(), Group.BONITA.getDisplayName(), Group.BONITA.getParentPath(), Role.EMPLOYEE.name()));


        Arrays.asList(Group.values()).stream().forEach(group -> {
            organization.getGroups().add(createGroup(group.getDisplayName(), group.getParentPath()));
            if (group.getLevel() == LevelGroup.TEAM) {
                buildTeamUsersWithMemberShip(numberUserByTeamGroup, group, theBoss.getUserName());
            }
        });

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
        exportedUser.setPassword(fairy.person().getPassword());
        exportedUser.setProfessionalAddress(fairy.person().getEmail());
        exportedUser.setUserName(fairy.person().getUsername());
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

    public  void export(File file) throws Exception {
        marshaller.marshal(organization, new File(file, "orga.xml"));
    }
/*
    public static void pusblish(APIClient apiClient) throws Exception {
        IdentityAPI identityAPI = apiClient.getIdentityAPI();
        for (ExportedGroup group : organization.getGroups()) {
            identityAPI.createGroup(mapToGroupCreator(group));
        }

        for (ExportedRole role : organization.getRoles()) {
            identityAPI.createRole(mapToGroupCreator(role));
        }

        for (ExportedUser user : managerByUserName.values()) {
            UserCreator userCreator = mapToUserCreator(user);
            identityAPI.createUser(userCreator);
            if(user.getManagerUserName() != null){
                User user1 = identityAPI.createUser(mapToUserCreator(user));
                managerIdByUSerName.put(user1.getUserName(),user1.getId());
                userCreator.setManagerUserId()
               
                if(user.get)
            }

        }

        for (ExportedUser user : organization.getUsers()) {
            identityAPI.createUser(mapToUserCreator(user));

           
        }
    }

    private static UserCreator mapToUserCreator(ExportedUser user) {
        UserCreator userCreator=new UserCreator(user.getUserName(),user.getPassword());
        userCreator.setFirstName(user.getFirstName());
        userCreator.setLastName(user.getLastName());
        ContactDataCreator contactDataCreator =new ContactDataCreator();
        contactDataCreator.setAddress(user.getProfessionalAddress());
        userCreator.setProfessionalContactData(contactDataCreator);
        userCreator.setManagerUserId(user.getManagerUserName());
        return  userCreator;
    }

    private static RoleCreator mapToGroupCreator(ExportedRole role) {
        RoleCreator roleCreator=new RoleCreator(role.getName());
        roleCreator.setDisplayName(role.getDisplayName());
        roleCreator.setDescription(role.getDescription());
        return roleCreator;
    }

    private static GroupCreator mapToGroupCreator(ExportedGroup exportedGroup) {
        GroupCreator groupCreator=new GroupCreator(exportedGroup.getName());
        groupCreator.setDisplayName(exportedGroup.getDisplayName());
        groupCreator.setDescription(exportedGroup.getDescription());
        groupCreator.setParentPath(exportedGroup.getParentPath());
        return groupCreator;
    }*/

}
