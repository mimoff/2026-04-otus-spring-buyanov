package ru.otus.hw.services;

import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
public class AclServiceWrapperServiceImpl implements AclServiceWrapperService {

    private final MutableAclService mutableAclService;

    public AclServiceWrapperServiceImpl(MutableAclService mutableAclService) {
        this.mutableAclService = mutableAclService;
    }

    @Override
    public void grantPermissions(Object domainObject, Permission... permissions) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var owner = new PrincipalSid(authentication);
        var objectIdentity = new ObjectIdentityImpl(domainObject);

        MutableAcl acl;
        try {
            acl = (MutableAcl) mutableAclService.readAclById(objectIdentity);
        } catch (NotFoundException e) {
            acl = mutableAclService.createAcl(objectIdentity);
        }

        for (var permission : permissions) {
            acl.insertAce(acl.getEntries().size(), permission, owner, true);
        }

        mutableAclService.updateAcl(acl);
    }

    @Override
    public void deleteAcl(Class<?> domainType, Serializable id) {
        mutableAclService.deleteAcl(new ObjectIdentityImpl(domainType, id), true);
    }

}
