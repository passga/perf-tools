package org.bonitasoft.audit.process;

import static org.bonitasoft.audit.process.OrgaGenerator.*;
import static org.bonitasoft.audit.process.OrgaGenerator.LevelGroup.TEAM;

import java.util.Arrays;

import jdk.Exported;
import org.bonitasoft.audit.process.OrgaGenerator.Group;
import org.bonitasoft.audit.process.xml.ExportedGroup;
import org.bonitasoft.audit.process.xml.ExportedRole;
import org.bonitasoft.audit.process.xml.ExportedUser;
import org.bonitasoft.audit.process.xml.ExportedUserMembership;
import org.bonitasoft.audit.process.xml.Organization;
import org.bonitasoft.engine.bpm.bar.actorMapping.Actor;
import org.bonitasoft.engine.bpm.bar.actorMapping.ActorMapping;

public class ActorMappingGenerator {

    private OrgaGenerator orgaGenerator;
    ActorMapping actorMapping;

    public static ActorMappingGenerator crateInstance() throws Exception {
        return new ActorMappingGenerator();
    }

    ActorMappingGenerator() throws Exception {
        this.orgaGenerator = OrgaGenerator.getInstance();
        actorMapping = new ActorMapping();


    }

    public ActorMapping build() {
        return actorMapping;
    }


    public ActorMappingGenerator withMemberActorMapping() {
        Actor actor = new Actor("Member actor");

        Arrays.asList(Group.values()).stream().filter(group -> group.getLevel() == TEAM).forEach(group -> {
            actor.getGroups().add(group.getParentPath() + "/" + group.getDisplayName());
        });
        actorMapping.getActors().add(actor);

        return this;
    }


    public ActorMappingGenerator withEmployeeActor() {
        Actor actor = new Actor("Employee actor");
        actor.addGroup("/" + Group.BONITA.getDisplayName());
        actorMapping.getActors().add(actor);
        return this;
    }


    public ActorMappingGenerator withInitiatorActor() {
        Actor actor = new Actor("Initiator");

        for (ExportedGroup group : orgaGenerator.getOrganization().getGroups()) {
            if (group.getParentPath() != null) {
                actor.addGroup(convertToActorGroup(group.getParentPath(), group.getName()));
            }
        }
        for (ExportedUser user : orgaGenerator.getOrganization().getUsers()) {
            actor.addUser(user.getUserName());
        }

        for (ExportedRole role : orgaGenerator.getOrganization().getRoles()) {
            actor.addRole(role.getName());
        }

        for (ExportedUserMembership userMembership : orgaGenerator.getOrganization().getMemberships()) {
            actor.addMembership(convertToActorGroup(userMembership.getGroupParentPath(), userMembership.getGroupName()), userMembership.getRoleName());
        }
        actorMapping.getActors().add(actor);
        return this;
    }


    public ActorMappingGenerator generateActors(int nbActor) {
        for (int i = 0; i < nbActor; i++) {
            Actor actor = new Actor("Actor" + i);
            ExportedGroup randomGroup = orgaGenerator.getRandomGroup();
            actor.addGroup(convertToActorGroup(randomGroup.getParentPath(), randomGroup.getName()));

            ExportedGroup randomGroup1 = orgaGenerator.getRandomGroup();
            actor.addMembership(convertToActorGroup(randomGroup.getParentPath(), randomGroup.getName()), orgaGenerator.getRandomRole().getName());
            actor.addRole(orgaGenerator.getRandomRole().getName());
            actor.addUser(orgaGenerator.getRandomUser().getUserName());
            actorMapping.getActors().add(actor);
        }

        return this;
    }

    private String convertToActorGroup(String parentPath, String groupName) {

        if (parentPath == null) {
            parentPath = "";
        }
        return parentPath + "/" + groupName;

    }


}
