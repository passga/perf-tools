package org.bonitasoft.audit.process;

import static org.bonitasoft.audit.process.OrgaGenerator.*;
import static org.bonitasoft.audit.process.OrgaGenerator.LevelGroup.COMPANY;
import static org.bonitasoft.audit.process.OrgaGenerator.LevelGroup.TEAM;

import java.util.Arrays;

import org.bonitasoft.audit.process.OrgaGenerator.Group;
import org.bonitasoft.engine.bpm.bar.actorMapping.Actor;
import org.bonitasoft.engine.bpm.bar.actorMapping.ActorMapping;

public class ActorMappingGenerator {

    public static ActorMapping buildActorMapping() {

        ActorMapping actorMapping = new ActorMapping();

        actorMapping.getActors().add(buildMemberActorMapping());
        actorMapping.getActors().add(buildEmployeeActorMapping());
        return actorMapping;
    }

    private static Actor buildMemberActorMapping() {
        Actor actor = new Actor(Role.MEMBER.name() + " Actor");

        Arrays.asList(Group.values()).stream().filter(group -> group.getLevel() == TEAM).forEach(group -> {
            actor.getGroups().add(group.getParentPath() + "/" + group.getDisplayName());
        });
        return actor;
    }

    private static Actor buildEmployeeActorMapping() {
        Actor actor = new Actor(Role.EMPLOYEE.name() + " Actor");
        actor.addGroup("/" + Group.BONITA.getDisplayName());
        return actor;
    }


}
