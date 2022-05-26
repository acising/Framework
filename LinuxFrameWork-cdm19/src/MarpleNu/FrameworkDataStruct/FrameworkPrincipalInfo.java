package MarpleNu.FrameworkDataStruct;

import com.bbn.tc.schema.avro.cdm19.*;


import java.util.List;

public class FrameworkPrincipalInfo {
    private UUID uuid;
    private CharSequence userId;
    private CharSequence username;
    private List<CharSequence> groupIds;
    private PrincipalType type;
    public FrameworkPrincipalInfo(Principal principal)
    {
        this.uuid=principal.getUuid();
        this.userId=principal.getUserId();
        this.username=principal.getUsername();
        this.groupIds=principal.getGroupIds();
        this.type=principal.getType();
    }

}
