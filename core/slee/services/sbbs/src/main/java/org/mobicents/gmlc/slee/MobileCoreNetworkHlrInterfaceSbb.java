/**
 * TeleStax, Open Source Cloud Communications  Copyright 2012. 
 * and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.mobicents.gmlc.slee;

import java.io.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.slee.ActivityContextInterface;
import javax.slee.CreateException;
import javax.slee.EventContext;
import javax.slee.RolledBackContext;
import javax.slee.Sbb;
import javax.slee.SbbContext;
import javax.slee.facilities.Tracer;
import javax.slee.resource.ResourceAdaptorTypeID;

import net.java.slee.resource.http.events.HttpServletRequestEvent;

import org.mobicents.gmlc.HlrPropertiesManagement;
import org.mobicents.gmlc.slee.mlp.MLPException;
import org.mobicents.gmlc.slee.mlp.MLPRequest;
import org.mobicents.gmlc.slee.mlp.MLPResponse;
import org.mobicents.protocols.ss7.indicator.NatureOfAddress;
import org.mobicents.protocols.ss7.indicator.NumberingPlan;
import org.mobicents.protocols.ss7.indicator.RoutingIndicator;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContext;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContextName;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContextVersion;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.MAPParameterFactory;
import org.mobicents.protocols.ss7.map.api.MAPProvider;
import org.mobicents.protocols.ss7.map.api.errors.MAPErrorCode;
import org.mobicents.protocols.ss7.map.api.errors.MAPErrorMessage;
import org.mobicents.protocols.ss7.map.api.primitives.AddressNature;
import org.mobicents.protocols.ss7.map.api.primitives.CellGlobalIdOrServiceAreaIdOrLAI;
import org.mobicents.protocols.ss7.map.api.primitives.CellGlobalIdOrServiceAreaIdFixedLength;
import org.mobicents.protocols.ss7.map.api.primitives.CellGlobalIdOrServiceAreaIdOrLAI;
import org.mobicents.protocols.ss7.map.api.primitives.IMEI;
import org.mobicents.protocols.ss7.map.api.primitives.IMSI;

import org.mobicents.protocols.ss7.map.api.primitives.ISDNAddressString;
import org.mobicents.protocols.ss7.map.api.primitives.MAPExtensionContainer;

import org.mobicents.protocols.ss7.map.api.primitives.SubscriberIdentity;
import org.mobicents.protocols.ss7.map.api.service.mobility.MAPDialogMobility;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.RequestedInfo;

import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.GPRSMSClass;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.GeodeticInformation;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.GeographicalInformation;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.LocationInformation;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.LocationInformationEPS;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.LocationInformationGPRS;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.LocationNumberMap;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.MNPInfoRes;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.MSClassmark2;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.NotReachableReason;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.PSSubscriberState;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.ProvideSubscriberInfoRequest;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.ProvideSubscriberInfoResponse;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.SubscriberInfo;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.SubscriberState;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.SubscriberStateChoice;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.UserCSGInformation;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberManagement.DeleteSubscriberDataRequest;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberManagement.DeleteSubscriberDataResponse;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberManagement.InsertSubscriberDataRequest;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberManagement.InsertSubscriberDataResponse;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberManagement.LSAIdentity;


import org.mobicents.protocols.ss7.map.MAPParameterFactoryImpl;



import org.mobicents.protocols.ss7.map.primitives.ISDNAddressStringImpl;
import org.mobicents.protocols.ss7.map.primitives.SubscriberIdentityImpl;
import org.mobicents.protocols.ss7.map.service.mobility.subscriberInformation.RequestedInfoImpl;
import org.mobicents.protocols.ss7.sccp.impl.parameter.ParameterFactoryImpl;
import org.mobicents.protocols.ss7.sccp.parameter.GlobalTitle;
import org.mobicents.protocols.ss7.sccp.parameter.ParameterFactory;
import org.mobicents.protocols.ss7.sccp.parameter.SccpAddress;
import org.mobicents.slee.SbbContextExt;
import org.mobicents.slee.resource.map.MAPContextInterfaceFactory;

import org.mobicents.protocols.ss7.map.api.service.sms.MAPDialogSms;
import org.mobicents.protocols.ss7.map.api.service.sms.SendRoutingInfoForSMRequest;
import org.mobicents.protocols.ss7.map.api.service.sms.LocationInfoWithLMSI;

/**
 * 
 * @author amit bhayani
 * @author sergey vetyutnev
 */
public abstract class MobileCoreNetworkHlrInterfaceSbb implements Sbb {

	protected SbbContextExt sbbContext;

	private Tracer logger;

	protected MAPContextInterfaceFactory mapAcif;
	protected MAPProvider mapProvider;
	protected MAPParameterFactory mapParameterFactory;
	protected ParameterFactory sccpParameterFact;

	protected static final ResourceAdaptorTypeID mapRATypeID = new ResourceAdaptorTypeID("MAPResourceAdaptorType",
			"org.mobicents", "2.0");
	protected static final String mapRaLink = "MAPRA";

	private static final HlrPropertiesManagement gmlcPropertiesManagement = HlrPropertiesManagement.getInstance();

	private SccpAddress gmlcSCCPAddress = null;
	private MAPApplicationContext anyTimeEnquiryContext = null;

    /**
     * HTTP Request Types (GET or MLP)
     */
    private enum HttpRequestType {
        REST("rest"),
        MLP("mlp"),
        UNSUPPORTED("404");

        private String path;

        HttpRequestType(String path) {
            this.path = path;
        }

        public String getPath() {
            return String.format("/gmlc/%s", path);
        }


        public static HttpRequestType fromPath(String path) {
            for (HttpRequestType type: values()) {
                if (path.equals(type.getPath())) {
                    return type;
                }
            }

            return UNSUPPORTED;
        }
    }

    /**
     * Request
     */
    private class HttpRequest implements Serializable {
        HttpRequestType type;
        String msisdn;

        public HttpRequest(HttpRequestType type, String msisdn) {
            this.type = type;
            this.msisdn = msisdn;
        }

        public HttpRequest(HttpRequestType type) {
            this(type, "");
        }
    }

    /**
     * Response Location
     */
    private class CGIResponse implements Serializable {
        String x = "-1";
        String y = "-1";
        String radius = "-1";
        int cell = -1;
        int mcc = -1;
        int mnc = -1;
        int lac = -1;
        int aol = -1;
        String vlr = "-1";
    }

    /**
     * For debugging - fake location data
     */
    private String fakeNumber = "19395550113";
    private MLPResponse.MLPResultType fakeLocationType = MLPResponse.MLPResultType.OK;
    private String fakeLocationAdditionalInfoErrorString = "Internal positioning failure occurred";
    private int fakeCellId = 300;
    private String fakeLocationX = "27 28 25.00S";
    private String fakeLocationY = "153 01 43.00E";
    private String fakeLocationRadius = "5000";

	/** Creates a new instance of CallSbb */
	public MobileCoreNetworkHlrInterfaceSbb() {
	}

	public void setSbbContext(SbbContext sbbContext) {
		this.sbbContext = (SbbContextExt) sbbContext;
		this.logger = sbbContext.getTracer(MobileCoreNetworkHlrInterfaceSbb.class.getSimpleName());
		try {
			this.mapAcif = (MAPContextInterfaceFactory) this.sbbContext.getActivityContextInterfaceFactory(mapRATypeID);
			this.mapProvider = (MAPProvider) this.sbbContext.getResourceAdaptorInterface(mapRATypeID, mapRaLink);

			this.mapParameterFactory = this.mapProvider.getMAPParameterFactory();
			this.sccpParameterFact = new ParameterFactoryImpl();
			//this.logger.info("setSbbContext() complete");
		} catch (Exception ne) {
			logger.severe("Could not set SBB context:", ne);
		}
	}

	public void unsetSbbContext() {
		this.sbbContext = null;
		this.logger = null;
	}

	public void sbbCreate() throws CreateException {
		if (this.logger.isFineEnabled()) {
			this.logger.fine("Created KnowledgeBase");
		}
	}

	public void sbbPostCreate() throws CreateException {

	}

	public void sbbActivate() {
	}

	public void sbbPassivate() {
	}

	public void sbbLoad() {
	}

	public void sbbStore() {
	}

	public void sbbRemove() {
	}

	public void sbbExceptionThrown(Exception exception, Object object, ActivityContextInterface activityContextInterface) {
	}

	public void sbbRolledBack(RolledBackContext rolledBackContext) {
	}

	/**
	 * DIALOG Events
	 */

	/*
public void onDialogRequest(org.mobicents.slee.resource.map.events.DialogRequest evt, ActivityContextInterface aci) {
		if (logger.isInfoEnabled()) {
			this.logger.info("New MAP Dialog. Received event MAPOpenInfo " + evt);
		}

	}
	*/





	public void onDialogTimeout(org.mobicents.slee.resource.map.events.DialogTimeout evt, ActivityContextInterface aci) {
        this.logger.severe("\nRx :  onDialogTimeout " + evt);

	//HLR	this.handleLocationResponse(MLPResponse.MLPResultType.SYSTEM_FAILURE, null, "DialogTimeout");
	}

	public void onDialogDelimiter(org.mobicents.slee.resource.map.events.DialogDelimiter event,
			ActivityContextInterface aci/* , EventContext eventContext */) {
     //   if (this.logger.isFineEnabled()) {
            this.logger.info("\nReceived onDialogDelimiter = " + event);
      //  }
	}

	public void onDialogAccept(org.mobicents.slee.resource.map.events.DialogAccept event, ActivityContextInterface aci) {
        //if (this.logger.isFineEnabled()) {
            this.logger.info("\nReceived onDialogAccept = " + event);
        //}
	}

	public void onDialogReject(org.mobicents.slee.resource.map.events.DialogReject event, ActivityContextInterface aci) {
        this.logger.severe("\nRx :  onDialogReject " + event);

        //HLR this.handleLocationResponse(MLPResponse.MLPResultType.SYSTEM_FAILURE, null, "DialogReject: " + event);
	}

	public void onDialogUserAbort(org.mobicents.slee.resource.map.events.DialogUserAbort event,
			ActivityContextInterface aci/* , EventContext eventContext */) {
        this.logger.severe("\nRx :  onDialogUserAbort " + event);

        //HLR this.handleLocationResponse(MLPResponse.MLPResultType.SYSTEM_FAILURE, null, "DialogUserAbort: " + event);
	}

	public void onDialogProviderAbort(org.mobicents.slee.resource.map.events.DialogProviderAbort event,
			ActivityContextInterface aci/* , EventContext eventContext */) {
        this.logger.severe("\nRx :  onDialogProviderAbort " + event);

       //HLR this.handleLocationResponse(MLPResponse.MLPResultType.SYSTEM_FAILURE, null, "DialogProviderAbort: " + event);
	}

	public void onDialogClose(org.mobicents.slee.resource.map.events.DialogClose event, ActivityContextInterface aci) {
        if (this.logger.isFineEnabled()) {
            this.logger.fine("\nReceived onDialogClose = " + event);
        }
	}

	public void onDialogNotice(org.mobicents.slee.resource.map.events.DialogNotice event, ActivityContextInterface aci) {
        if (this.logger.isFineEnabled()) {
            this.logger.fine("\nReceived onDialogNotice = " + event);
        }
	}

	public void onDialogRelease(org.mobicents.slee.resource.map.events.DialogRelease event, ActivityContextInterface aci) {
        if (this.logger.isFineEnabled()) {
            this.logger.fine("\nReceived onDialogRelease = " + event);
        }
	}

	/**
	 * Component Events
	 */
	public void onInvokeTimeout(org.mobicents.slee.resource.map.events.InvokeTimeout event, ActivityContextInterface aci) {
        if (this.logger.isFineEnabled()) {
            this.logger.fine("\nReceived onInvokeTimeout = " + event);
        }
	}

	public void onErrorComponent(org.mobicents.slee.resource.map.events.ErrorComponent event,
			ActivityContextInterface aci/* , EventContext eventContext */) {
        if (this.logger.isFineEnabled()) {
            this.logger.fine("\nReceived onErrorComponent = " + event);
        }

		MAPErrorMessage mapErrorMessage = event.getMAPErrorMessage();
		long error_code = mapErrorMessage.getErrorCode().longValue();

        this.handleLocationResponse(
                (error_code == MAPErrorCode.unknownSubscriber ? MLPResponse.MLPResultType.UNKNOWN_SUBSCRIBER
                        : MLPResponse.MLPResultType.SYSTEM_FAILURE), null, "ReturnError: " + String.valueOf(error_code) + " : "
                        + event.getMAPErrorMessage());
	}

	public void onRejectComponent(org.mobicents.slee.resource.map.events.RejectComponent event,
			ActivityContextInterface aci/* , EventContext eventContext */) {
        this.logger.severe("\nRx :  onRejectComponent " + event);

        this.handleLocationResponse(MLPResponse.MLPResultType.SYSTEM_FAILURE, null, "RejectComponent: " + event);
	}

	/**
	 * ATI Events
	 */
	public void onAnyTimeInterrogationRequest(
			org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.AnyTimeInterrogationRequest event,
			ActivityContextInterface aci/* , EventContext eventContext */) {
        this.logger.info("\nReceived onAnyTimeInterrogationRequest = " + event + "\nDialog=" + (MAPDialogMobility)event.getMAPDialog());

		try {
			   long invokeId = event.getInvokeId();
	            MAPDialogMobility mapDialogMobility = event.getMAPDialog();
	            //mapDialogMobility.setUserObject(invokeId);
		// instead use CMP  this.setAnyTimeInterrogationRequestInvokeId(event.getInvokeId());

	            MAPParameterFactoryImpl mapFactory = new MAPParameterFactoryImpl();

	            // Create Subscriber Information parameters including Location Information and Subscriber State
	            // for concerning MAP operation
	            CellGlobalIdOrServiceAreaIdFixedLength cellGlobalIdOrServiceAreaIdFixedLength = mapFactory
	                    .createCellGlobalIdOrServiceAreaIdFixedLength(410, 03, 23, 369);
	            CellGlobalIdOrServiceAreaIdOrLAI cellGlobalIdOrServiceAreaIdOrLAI = mapFactory
	                    .createCellGlobalIdOrServiceAreaIdOrLAI(cellGlobalIdOrServiceAreaIdFixedLength);
	            ISDNAddressString vlrNumber = new ISDNAddressStringImpl(AddressNature.international_number,
	                    org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan.ISDN, "5982123007");
	            ISDNAddressString mscNumber = new ISDNAddressStringImpl(AddressNature.international_number,
	                    org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan.ISDN, "5982123007");
	            Integer ageOfLocationInformation = 0; // ageOfLocationInformation
	            GeographicalInformation geographicalInformation = null;
	            LocationNumberMap locationNumber = null;
	            MAPExtensionContainer mapExtensionContainer = null;
	            LSAIdentity selectedLSAId = null;
	            GeodeticInformation geodeticInformation = null;
	            boolean currentLocationRetrieved = false;
	            boolean saiPresent = false;
	            LocationInformationEPS locationInformationEPS = null;
	            UserCSGInformation userCSGInformation = null;
	            LocationInformationGPRS locationInformationGPRS = null;
	            PSSubscriberState psSubscriberState = null;
	            IMEI imei = null;
	            MSClassmark2 msClassmark2 = null;
	            GPRSMSClass gprsMSClass = null;
	            MNPInfoRes mnpInfoRes = null;
	            SubscriberStateChoice subscriberStateChoice = SubscriberStateChoice.assumedIdle; // 0=assumedIdle, 1=camelBusy, 2=notProvidedFromVLR
	            NotReachableReason notReachableReason = null;

	            LocationInformation locationInformation = mapFactory.createLocationInformation(ageOfLocationInformation,
	                    geographicalInformation, vlrNumber, locationNumber, cellGlobalIdOrServiceAreaIdOrLAI, mapExtensionContainer,
	                    selectedLSAId, mscNumber, geodeticInformation, currentLocationRetrieved, saiPresent, locationInformationEPS,
	                    userCSGInformation);

	            SubscriberState subscriberState = mapFactory.createSubscriberState(subscriberStateChoice, notReachableReason);

	            SubscriberInfo subscriberInfo = mapFactory.createSubscriberInfo(locationInformation, subscriberState,
	                    mapExtensionContainer, locationInformationGPRS, psSubscriberState, imei, msClassmark2, gprsMSClass,
	                    mnpInfoRes);

	            mapDialogMobility.addAnyTimeInterrogationResponse(invokeId, subscriberInfo, mapExtensionContainer);

	            // This will initiate the TC-BEGIN with INVOKE component
	            mapDialogMobility.close(false);

		} catch (MAPException mapException) {
			 logger.severe("MAP Exception while processing AnyTimeInterrogationRequest ", mapException);
		} catch (Exception e) {
			  logger.severe("Exception while processing AnyTimeInterrogationRequest ", e);
		}






	}

	public void onAnyTimeInterrogationResponse(
			org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.AnyTimeInterrogationResponse event,
			ActivityContextInterface aci/* , EventContext eventContext */) {
	        //if (this.logger.isFineEnabled()) {
	            this.logger.info("\nReceived onAnyTimeInterrogationResponse = " + event);
	        //}


	}


	
	public void onSendRoutingInfoForSMRequest(SendRoutingInfoForSMRequest event,
		ActivityContextInterface aci) {
        this.logger.info("\nReceived SendRoutingInfoForSMRequest = " + event + "\nDialog=" + (MAPDialogSms)event.getMAPDialog());
	MAPDialogSms dialog = event.getMAPDialog();
	long invokeId = event.getInvokeId();
	MAPParameterFactoryImpl mapFactory = new MAPParameterFactoryImpl();
	IMSI imsi = mapFactory.createIMSI("555667");

	 ISDNAddressString networkNodeNumber = mapFactory
	 .createISDNAddressString(AddressNature.international_number, 
		org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan.ISDN, "8786876");
        LocationInfoWithLMSI li = null;
	li = mapFactory.createLocationInfoWithLMSI(networkNodeNumber, null, null, false, null);
        try {
		dialog.addSendRoutingInfoForSMResponse(invokeId, imsi, li, null, null);
	        dialog.close(false);

	} catch (MAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	}
}
	

    /**
     * Handle HTTP POST request
     * @param event
     * @param aci
     * @param eventContext
     */
    public void onPost(net.java.slee.resource.http.events.HttpServletRequestEvent event, ActivityContextInterface aci,
                      EventContext eventContext) {
        onRequest(event, aci, eventContext);
    }

    /**
     * Handle HTTP GET request
     * @param event
     * @param aci
     * @param eventContext
     */
	public void onGet(net.java.slee.resource.http.events.HttpServletRequestEvent event, ActivityContextInterface aci,
			EventContext eventContext) {
        onRequest(event, aci, eventContext);
	}

    /**
     * Entry point for all location lookups
     * Assigns a protocol handler to the request based on the path
     */
    private void onRequest(net.java.slee.resource.http.events.HttpServletRequestEvent event, ActivityContextInterface aci,
                           EventContext eventContext) {
        setEventContext(eventContext);
        HttpServletRequest httpServletRequest = event.getRequest();
        HttpRequestType httpRequestType = HttpRequestType.fromPath(httpServletRequest.getPathInfo());
        setHttpRequest(new HttpRequest(httpRequestType));
        String requestingMSISDN = null;

	logger.info("onRequest()="+httpRequestType);
        switch (httpRequestType) {
            case REST:
                requestingMSISDN = httpServletRequest.getParameter("msisdn");
                break;
            case MLP:
                try {
                    // Get the XML request from the POST data
                    InputStream body = httpServletRequest.getInputStream();
                    // Parse the request and retrieve the requested MSISDN
                    MLPRequest mlpRequest = new MLPRequest(logger);
                    requestingMSISDN = mlpRequest.parseRequest(body);
                } catch(MLPException e) {
                    handleLocationResponse(e.getMlpClientErrorType(), null, "System Failure: " + e.getMlpClientErrorMessage());
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                    handleLocationResponse(MLPResponse.MLPResultType.FORMAT_ERROR, null, "System Failure: Failed to read from server input stream");
                    return;
                }
                break;
            default:
                // Silence sendHTTPResult(HttpServletResponse.SC_NOT_FOUND, "Request URI unsupported");
                return;
        }

        setHttpRequest(new HttpRequest(httpRequestType, requestingMSISDN));
        if (logger.isFineEnabled()){
            logger.fine(String.format("Handling %s request, MSISDN: %s", httpRequestType.name().toUpperCase(), requestingMSISDN));
        }

        if (requestingMSISDN != null) {
            eventContext.suspendDelivery();
            getSingleMSISDNLocation(requestingMSISDN);
        } else {
            logger.info("MSISDN is null, sending back -1 for Global Cell Identity");
            handleLocationResponse(MLPResponse.MLPResultType.FORMAT_ERROR, null, "Invalid MSISDN specified");
        }
    }

	/**
	 * CMP
	 */
	public abstract void setEventContext(EventContext cntx);

	public abstract EventContext getEventContext();

    public abstract void setHttpRequest(HttpRequest httpRequest);

    public abstract HttpRequest getHttpRequest();

	/**
	 * Private helper methods
	 */

    /**
     * Retrieve the location for the specified MSISDN via ATI request to the HLR
     */
    private void getSingleMSISDNLocation(String requestingMSISDN) {
        if (!requestingMSISDN.equals(fakeNumber)) {
            try {
                MAPDialogMobility mapDialogMobility = this.mapProvider.getMAPServiceMobility().createNewDialog(
                        this.getSRIMAPApplicationContext(), this.getGmlcSccpAddress(), null,
                        getHlrSCCPAddress(requestingMSISDN), null);

                ISDNAddressString isdnAdd = new ISDNAddressStringImpl(AddressNature.international_number,
                        org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan.ISDN, requestingMSISDN);
                SubscriberIdentity subsId = new SubscriberIdentityImpl(isdnAdd);
                RequestedInfo requestedInfo = new RequestedInfoImpl(true, true, null, false, null, false, false, false);
                // requestedInfo (MAP ATI): last known location and state (idle or busy), no IMEI/MS Classmark/MNP
			
                ISDNAddressString gscmSCFAddress = new ISDNAddressStringImpl(AddressNature.international_number,
                        org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan.ISDN,
                        gmlcPropertiesManagement.getGmlcGt());

                mapDialogMobility.addAnyTimeInterrogationRequest(subsId, requestedInfo, gscmSCFAddress, null);

                ActivityContextInterface sriDialogACI = this.mapAcif.getActivityContextInterface(mapDialogMobility);
                sriDialogACI.attach(this.sbbContext.getSbbLocalObject());
                mapDialogMobility.send();
            } catch (MAPException e) {
                this.logger.severe("MAPException while trying to send ATI request for MSISDN=" + requestingMSISDN, e);
                this.handleLocationResponse(MLPResponse.MLPResultType.SYSTEM_FAILURE, null,
                        "System Failure: Failed to send request to network for position: " + e.getMessage());
            } catch (Exception e) {
                this.logger.severe("Exception while trying to send ATI request for MSISDN=" + requestingMSISDN, e);
                this.handleLocationResponse(MLPResponse.MLPResultType.SYSTEM_FAILURE, null,
                        "System Failure: Failed to send request to network for position: " + e.getMessage());
            }
        }
        else {
            // Handle fake success
            if (this.fakeLocationType == MLPResponse.MLPResultType.OK) {
                CGIResponse response = new CGIResponse();
                response.cell = fakeCellId;
                response.x = fakeLocationX;
                response.y = fakeLocationY;
                response.radius = fakeLocationRadius;
                this.handleLocationResponse(MLPResponse.MLPResultType.OK, response, null);
            }
            else {
                this.handleLocationResponse(this.fakeLocationType, null, this.fakeLocationAdditionalInfoErrorString);
            }
        }
    }

	protected SccpAddress getGmlcSccpAddress() {
		if (this.gmlcSCCPAddress == null) {
				// GmlcGT
            GlobalTitle gt = sccpParameterFact.createGlobalTitle(gmlcPropertiesManagement.getGmlcGt(), 0,
                    NumberingPlan.ISDN_TELEPHONY, null, NatureOfAddress.INTERNATIONAL);
            this.gmlcSCCPAddress = sccpParameterFact.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE,
                    gt, 0, gmlcPropertiesManagement.getGmlcSsn());

//			GlobalTitle0100 gt = new GlobalTitle0100Impl(gmlcPropertiesManagement.getGmlcGt(),0,BCDEvenEncodingScheme.INSTANCE,NumberingPlan.ISDN_TELEPHONY,NatureOfAddress.INTERNATIONAL);
//			this.serviceCenterSCCPAddress = new SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, gt, 0, gmlcPropertiesManagement.getGmlcSsn());
		}
		return this.gmlcSCCPAddress;
	}

	private MAPApplicationContext getSRIMAPApplicationContext() {
		if (this.anyTimeEnquiryContext == null) {
			this.anyTimeEnquiryContext = MAPApplicationContext.getInstance(
					MAPApplicationContextName.anyTimeEnquiryContext, MAPApplicationContextVersion.version3);
		}
		return this.anyTimeEnquiryContext;
	}

	private SccpAddress getHlrSCCPAddress(String address) {
        GlobalTitle gt = sccpParameterFact.createGlobalTitle(address, 0, NumberingPlan.ISDN_TELEPHONY, null,
                NatureOfAddress.INTERNATIONAL);
        return sccpParameterFact.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, gt, 0,
                gmlcPropertiesManagement.getHlrSsn());

//	    GlobalTitle0100 gt = new GlobalTitle0100Impl(address, 0, BCDEvenEncodingScheme.INSTANCE,NumberingPlan.ISDN_TELEPHONY, NatureOfAddress.INTERNATIONAL);
//		return new SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, gt, 0, gmlcPropertiesManagement.getHlrSsn());
	}

    /**
     * Handle generating the appropriate HTTP response
     * We're making use of the MLPResponse class for both GET/POST requests for convenience and
     * because eventually the GET method will likely be removed
     * @param mlpResultType OK or error type to return to client
     * @param response CGIResponse on location attempt
     * @param mlpClientErrorMessage Error message to send to client
     */
    private void handleLocationResponse(MLPResponse.MLPResultType mlpResultType, CGIResponse response, String mlpClientErrorMessage) {
        HttpRequest request = getHttpRequest();

        switch(request.type)
        {
            case REST:
                if (mlpResultType == MLPResponse.MLPResultType.OK) {
                    
                	StringBuilder getResponse = new StringBuilder();
					getResponse.append("mcc=");
					getResponse.append(response.mcc);
					getResponse.append(",mnc=");
					getResponse.append(String.format("%02d",response.mnc));
					getResponse.append(",lac=");
					getResponse.append(response.lac);
					getResponse.append(",cellid=");
					getResponse.append(response.cell);
					getResponse.append(",aol=");
					getResponse.append(response.aol);
					getResponse.append(",vlrNumber=");
					getResponse.append(response.vlr);

                    this.sendHTTPResult(HttpServletResponse.SC_OK, getResponse.toString());
                }
                else {
                    this.sendHTTPResult(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, mlpClientErrorMessage);
                }
                break;

            case MLP:
                String svcResultXml;
                MLPResponse mlpResponse = new MLPResponse(this.logger);

                if (mlpResultType == MLPResponse.MLPResultType.OK) {
                    svcResultXml = mlpResponse.getSinglePositionSuccessXML(response.x, response.y, response.radius, request.msisdn);
                }
                else if (MLPResponse.isSystemError(mlpResultType)) {
                    svcResultXml = mlpResponse.getSystemErrorResponseXML(mlpResultType, mlpClientErrorMessage);
                }
                else {
                    svcResultXml = mlpResponse.getPositionErrorResponseXML(request.msisdn, mlpResultType, mlpClientErrorMessage);
                }

                this.sendHTTPResult(HttpServletResponse.SC_OK, svcResultXml);
                break;
        }
    }

    /**
     * Return the specified response data to the HTTP client
     * @param responseData Response data to send to client
     */
	private void sendHTTPResult(int statusCode, String responseData) {
		try {
			EventContext ctx = this.getEventContext();
            if (ctx == null) {
                if (logger.isWarningEnabled()) {
                    logger.warning("When responding to HTTP no pending HTTP request is found, responseData=" + responseData);
                    return;
                }
            }

	        HttpServletRequestEvent event = (HttpServletRequestEvent) ctx.getEvent();

			HttpServletResponse response = event.getResponse();
                        response.setStatus(statusCode);
            PrintWriter w = null;
            w = response.getWriter();
            w.print(responseData);
			w.flush();
			response.flushBuffer();

			if (ctx.isSuspended()) {
				ctx.resumeDelivery();
			}

			if (logger.isFineEnabled()){
			    logger.fine("HTTP Request received and response sent, responseData=" + responseData);
			}

			// getNullActivity().endActivity();
		} catch (Exception e) {
			logger.severe("Error while sending back HTTP response", e);
		}
	}
}
