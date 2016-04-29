package org.orderofthebee.alfresco.repo.behaviour;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

public class InitialVersionContentBehaviour implements NodeServicePolicies.OnCreateNodePolicy {

	private PolicyComponent policyComponent;
	private NodeService nodeService;
	private NamespaceService namespaceService;
	private Properties globalProperties;

	public void init() {

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, ContentModel.TYPE_CMOBJECT,
				new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT));
	}

	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {

		NodeRef nodeRef = childAssocRef.getChildRef();
		if (!nodeService.exists(nodeRef)) {
			return;
		}

		if (StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.equals(nodeRef.getStoreRef())) {
			QName type = nodeService.getType(nodeRef);
			String localName = type.getLocalName();
			// may be empty but it shouldn't
			String prefix = namespaceService.getPrefixes(type.getNamespaceURI()).iterator().next(); 

			final String initialVersionKey = MessageFormat.format("initialVersionControl.{0}_{1}.initalVersion.enabled",
					prefix, localName);
			final Object initialVersionValue = this.globalProperties.get(initialVersionKey);
			if (initialVersionValue instanceof String && !((String) initialVersionValue).isEmpty()
					&& Boolean.parseBoolean((String) initialVersionValue)) {

				if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE)) {

					Map<QName, Serializable> versionProperties = null;

					String autoVersionKey = MessageFormat.format("initialVersionControl.{0}_{1}.autoVersion.enabled",
							prefix, localName);
					Object autoVersionValue = this.globalProperties.get(autoVersionKey);
					if (autoVersionValue instanceof String && !((String) autoVersionValue).isEmpty()) {
						versionProperties = new HashMap<QName, Serializable>();
						versionProperties.put(ContentModel.PROP_AUTO_VERSION, Boolean.parseBoolean((String) autoVersionValue));
					}

					String autoVersionOnPropertiesUpdateKey = MessageFormat.format(
							"initialVersionControl.{0}_{1}.autoVersionOnPropertiesUpdate.enabled", prefix, localName);
					Object autoVersionOnPropertiesUpdateValue = this.globalProperties
							.get(autoVersionOnPropertiesUpdateKey);
					if (autoVersionOnPropertiesUpdateValue instanceof String && !((String) autoVersionOnPropertiesUpdateValue).isEmpty()) {

						if (versionProperties == null) {
							versionProperties = new HashMap<QName, Serializable>();
						}
						versionProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, Boolean.parseBoolean((String) autoVersionOnPropertiesUpdateValue));
					}

					nodeService.addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, versionProperties);
				}

			}

		}

	}

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setGlobalProperties(Properties globalProperties) {
		this.globalProperties = globalProperties;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}
}
