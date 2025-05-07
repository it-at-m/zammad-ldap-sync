package de.muenchen.zammad.ad;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class ActiveDirectoryUserDTO implements Serializable{

    @Serial
    private static final long serialVersionUID = 1L;
    private String cn;
    private String lhmObjectId;
    private String distinguishedName;
    private String displayName;
    private String name;
    private String uid;
    private String lhmReferatName;

}
