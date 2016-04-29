package org.orderofthebee.alfresco.repo.behaviour;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeRef.Status;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;

public class InitialVersionContentBehaviour implements ContentServicePolicies.OnContentUpdatePolicy  {
	
	private PolicyComponent policyComponent;
	private NodeService nodeService;
	private VersionService versionService;
	
	public void init() {
		policyComponent.bindClassBehaviour(
				ContentServicePolicies.OnContentUpdatePolicy.QNAME,
				ContentModel.TYPE_CONTENT,
				new JavaBehaviour(
						this,
						"onContentUpdate",
						NotificationFrequency.TRANSACTION_COMMIT));
	}

	@Override
	public void onContentUpdate(NodeRef nodeRef, boolean newContent) {
		
		Status nodeStatus = nodeService.getNodeStatus(nodeRef);
		if (nodeStatus != null && !nodeStatus.isDeleted()) 
		{
			// Create initial version
			Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
			versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
			
			if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE)) {
				Map<QName, Serializable> versionProps = new HashMap<QName, Serializable>();
				versionProps.put(ContentModel.PROP_AUTO_VERSION_PROPS, new Boolean(false));
				nodeService.addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, versionProps);
			}
			
			// Marked as creating operation to avoid versioning behaviours to be applied
			versionService.createVersion(nodeRef, versionProperties);
		}
		
		
	}

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setVersionService(VersionService versionService) {
		this.versionService = versionService;
	}

	
}
