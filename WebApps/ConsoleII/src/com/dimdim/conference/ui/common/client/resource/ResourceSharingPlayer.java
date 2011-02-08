/*
 **************************************************************************
 *                                                                        *
 *               DDDDD   iii             DDDDD   iii                      *
 *               DD  DD      mm mm mmmm  DD  DD      mm mm mmmm           *
 *               DD   DD iii mmm  mm  mm DD   DD iii mmm  mm  mm          *
 *               DD   DD iii mmm  mm  mm DD   DD iii mmm  mm  mm          *
 *               DDDDDD  iii mmm  mm  mm DDDDDD  iii mmm  mm  mm          *
 *                                                                        *
 **************************************************************************
 **************************************************************************
 *                                                                        *
 * Part of the DimDim V 1.0 Codebase (http://www.dimdim.com)	          *
 *                                                                        *
 * Copyright (c) 2006 Communiva Inc. All Rights Reserved.                 *
 *                                                                        *
 *                                                                        *
 * This code is licensed under the DimDim License                         *
 * For details please visit http://www.dimdim.com/license                 *
 *                                                                        *
 **************************************************************************
 */

package com.dimdim.conference.ui.common.client.resource;

import java.util.Vector;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.dimdim.conference.ui.json.client.UIPopoutPanelData;
import com.dimdim.conference.ui.json.client.UIRosterEntry;
import com.dimdim.conference.ui.json.client.UIResourceObject;
import com.dimdim.conference.ui.json.client.UIStreamControlEvent;
import com.dimdim.conference.ui.json.client.UIPresentationControlEvent;
import com.dimdim.conference.ui.json.client.ResponseAndEventReader;
import com.dimdim.conference.ui.json.client.UIWhiteboardControlEvent;

import com.dimdim.conference.ui.model.client.ClientModel;
import com.dimdim.conference.ui.model.client.JSInterface;
import com.dimdim.conference.ui.model.client.JSCallbackListener;
import com.dimdim.conference.ui.model.client.SharingModelListener;
import com.dimdim.conference.ui.model.client.PPTSharingModelListener;
import com.dimdim.conference.ui.model.client.ConferenceGlobals;
import com.dimdim.conference.ui.model.client.PopoutPanelProxy;
import com.dimdim.conference.ui.model.client.PopoutSupportingPanel;
import com.dimdim.conference.ui.model.client.PPTSharingModel;
import com.dimdim.conference.ui.model.client.LocalEventsModel;
import com.dimdim.conference.ui.model.client.WhiteboardModelListener;

import com.dimdim.conference.ui.common.client.util.DebugPanel;
import com.dimdim.conference.ui.common.client.util.DmFlashWidget2;

import com.dimdim.conference.ui.common.client.UIGlobals;
import com.dimdim.conference.ui.common.client.ResourceGlobals;
import com.dimdim.conference.ui.common.client.LayoutGlobals;
import com.dimdim.conference.ui.common.client.UIConstants;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;

/**
 * @author Jayant Pandit
 * @email Jayant.Pandit@communiva.com
 * 
 * 05-September-2007 powerpoint movie scaling paradigm - With the new
 * swf slides generated by dms can be scaled any way. Hence there is no
 * reason to enforce any specific size on the player. However the player
 * is inside a frame, which is in the available area in the workspace and
 * must be made to fit the area in order to avoid scrollbars. Also the
 * movie inside the frame must be fit properly to avoid scrollbars on the
 * frame page.
 * 
 * The final paradigm is this:
 * 
 * 1.	The frame will be exactly the size of the available area at all
 * 		times. When the window is resized, in any way, the frame will be
 * 		resized and reloaded as well.
 * 2.	The frame size will be made available to the ppt module which loads
 * 		appropriate movie. This module will fit the movie within the frame
 * 		as per the most common scaling process.
 * 3.	Size of the movie area is essentially the size of the swf tag. This
 * 		area has the aspect ratio of 1.334. This ratio will be maintained.
 * 		The movie will be expanded till the available height or width is
 * 		reached, whichever is reached first.
 */

public class ResourceSharingPlayer //extends ComplexPanel	implements
//	SharingModelListener, PPTSharingModelListener, PopoutSupportingPanel, JSCallbackListener, WhiteboardModelListener
{
	/*
	protected	UIRosterEntry	me;
	protected	Element	outer;
	protected	ScrollPanel	scroller;
	
	protected	VerticalPanel	moviePanel = new VerticalPanel();
	protected	DmFlashWidget2	movie = null;
	protected	Frame			waitPageFrame = null;
	protected	ResponseAndEventReader	jsonReader = new ResponseAndEventReader();
	
	protected	int		lastKnownWidth;
	protected	int		lastKnownHeight;
	protected	boolean	shareMovie = false;
	protected	boolean	frameActive = false;
	
	protected	PPTSharingModel		pptSharingModel;
	protected	LocalEventsModel	localEventsModel;
	
	protected	Frame	dtpFrame	= null;
	
	protected	Frame	pptFrame	= null;
	protected	int		pptMaxWidth = 660;
	protected	int		pptMaxHeight = 495;
	
	protected	int		pptMaxWidthLarge = 924;
	protected	int		pptMaxHeightLarge = 693;
	
	protected	int		pptAspectRatio =  1334;
	protected	int		pptFrameWidth;
	protected	int		pptFrameHeight;
	
	protected	String	pptName;
	protected	String	pptId;
	protected	String	pptType;
	protected	String	annotation = UIPresentationControlEvent.ANNOTATION_OFF;
	protected	int		showSlide = 0;
	protected	int		numSlides = 0;
	protected	int		slideWidth = 0;
	protected	int		slideHeight = 0;
	
	protected	int		dtpMaxWidth = 1024;
	protected	int		dtpMaxHeight = 798;
	
	String movieUrl = "";
	
	String waitPageUrl = ConferenceGlobals.baseWebappURL+"share_wait/share_wait.html";
	
	public	ResourceSharingPlayer(UIRosterEntry me, int width, int height)
	{
		outer = DOM.createDiv();
		setElement(outer);
		DOM.setAttribute(outer,"id","ResourceSharingPlayer");
		setStyleName("resource-sharing-player");
		this.addStyleName("av-conference-panel");
		
		scroller = new ScrollPanel();
		scroller.setHeight(height+"px");
		scroller.setWidth(width+"px");
		super.add(scroller,outer);
		
		this.lastKnownWidth = width;
		this.lastKnownHeight = height;
		
		this.scroller.add(this.moviePanel);
		addWaitPage(width,height);
		
		this.me = me;
		
		pptSharingModel = ClientModel.getClientModel().getPPTSharingModel();
		localEventsModel = ClientModel.getClientModel().getLocalEventsModel();
		ClientModel.getClientModel().getSharingModel().addListener(this);
		ClientModel.getClientModel().getPPTSharingModel().addListener(this);
		ClientModel.getClientModel().getWhiteboardModel().addListener(this);
		
		JSInterface.addCallbackListener(this);
		
		String dtpProtocol = UIGlobals.getStreamingUrlsTable().getDtpProtocol();
		String dtpHost = UIGlobals.getStreamingUrlsTable().getDtpHost();
		String dtpPort = UIGlobals.getStreamingUrlsTable().getDtpPort();
		String dtpShareApp = UIGlobals.getStreamingUrlsTable().getDtpShareApp();
		String str = "p:"+dtpProtocol+",h:"+dtpHost+",p:"+dtpPort+",a:"+dtpShareApp;
		DebugPanel.getDebugPanel().addDebugMessage(str);
	}
	
	protected	void	setHeaderText(boolean dtpShare, String appName, String pptName)
	{
		Label headerLabel = LayoutGlobals.getWorkspaceHeaderLabel();
		if (headerLabel != null)
		{
			if (dtpShare)
			{
				//	Presenter is sharing desktop.
				headerLabel.setText(UIGlobals.getDTPSharingWorkspaceHeaderText());
			}
			else if (appName != null)
			{
				//	Presenter is sharing a specific application
				headerLabel.setText(UIGlobals.getAppSharingWorkspaceHeaderText(appName));
			}
			else if (pptName != null)
			{
				//	Presenter is sharing a powerpoint presentation
				headerLabel.setText(UIGlobals.getPptSharingWorkspaceHeaderText(pptName));
			}
			else
			{
				//	No sharing is in progress at present.
				headerLabel.setText(UIGlobals.getWorkspaceHeaderText());
			}
		}
	}
	public void onStartSharing(UIStreamControlEvent event)
	{
		if (UIGlobals.isInLobby(this.me))
		{
			return;
		}
		if (UIGlobals.isActivePresenter(this.me))
		{
			return;
		}
		this.showDTPPlayerFrame(event,false,false);
		/*
		this.shareMovie = true;
		if (this.movie != null)
		{
			moviePanel.remove(movie);
			movie = null;
		}
		if (this.waitPageFrame != null)
		{
			moviePanel.remove(this.waitPageFrame);
			this.waitPageFrame = null;
		}
		pptControlEvent = null;
		movieUrl = this.getMovieUrl(event);
		
		addMovie(movieUrl);
		*--/
		this.localEventsModel.screenShareStarted(this.lastKnownWidth,
				this.dtpMaxWidth,this.lastKnownHeight,this.dtpMaxHeight);
	}
//	private void addMovie(String movieUrl)
//	{
//	    movie = new DmFlashWidget2("player_movie","START",lastKnownWidth+"",lastKnownHeight+"",movieUrl,"white");
////		movie.addStyleName("resource-share-movie-top-margin");
//	    this.moviePanel.setSize(this.lastKnownWidth+"px", this.lastKnownHeight+"px");
//	    moviePanel.add(movie);
//	    movie.show();
//	}
	public void onStopSharing(UIStreamControlEvent event)
	{
		//Window.alert("inside onStopSharing..");
		if (UIGlobals.isInLobby(this.me))
		{
			return;
		}
		if (UIGlobals.isActivePresenter(this.me))
		{
			return;
		}
		stopDtpSharing();
//		this.shareMovie = false;
//		addWaitPage(this.lastKnownWidth,this.lastKnownHeight);
//		this.setHeaderText(false,null,null);
	}
	private void showDTPPlayerFrame(UIStreamControlEvent event,boolean onTabChange,boolean onResize)
	{
		if (UIGlobals.isInLobby(this.me))
		{
			return;
		}
		
		String movieUrl = this.getDTPMovieUrl(event);
		if (movieUrl != null)
		{
			this.shareMovie = true;
			if (this.movie != null)
			{
				moviePanel.remove(movie);
				movie = null;
			}
			if (this.waitPageFrame != null)
			{
				moviePanel.remove(this.waitPageFrame);
				this.waitPageFrame = null;
			}
			if (this.dtpFrame == null)
			{
				this.dtpFrame = new Frame();
			}
			if (!onTabChange && !onResize)
			{
				this.setFrameSize(this.lastKnownWidth,this.lastKnownHeight);
				this.dtpFrame.setStyleName("ppt-broadcaster-frame");
				this.scroller.remove(moviePanel);
				this.scroller.add(this.dtpFrame);
			}
			
			this.dtpFrame.setUrl(movieUrl);
			
			String resourceName = this.getResourceName(event.getResourceId());
			boolean isDesktop = false;
			String resourceId = event.getResourceId();
			UIResourceObject res = ClientModel.getClientModel().getResourceModel().findResourceObject(resourceId);
			if (res.getResourceType().equals(UIConstants.RESOURCE_TYPE_DESKTOP))
			{
			    isDesktop = true;
			}
			this.setHeaderText(isDesktop,null,resourceName);
		}
		else
		{
//			Window.alert("Failed to start ppt sharing:"+event.getResourceId());
		}
	}
	public void stopDtpSharing()
	{
		if (UIGlobals.isInLobby(this.me))
		{
			return;
		}
//		this.removePptSharingControl();
		this.scroller.remove(this.dtpFrame);
		this.scroller.add(this.moviePanel);
		this.shareMovie = false;
		this.dtpFrame = null;
		addWaitPage(this.lastKnownWidth,this.lastKnownHeight);
		this.setHeaderText(false,null,null);
	}
	public void onChangeShare(UIStreamControlEvent event)
	{
		String resourceId = event.getResourceId();
		UIResourceObject res = ClientModel.getClientModel().
				getResourceModel().findResourceObject(resourceId);
		if (res != null)
		{
			if (res.getResourceType().equals(UIConstants.RESOURCE_TYPE_DESKTOP))
			{
				this.setHeaderText(true,null,null);
			}
			else
			{
				String resourceName = this.getResourceName(event.getResourceId());
				this.setHeaderText(false,resourceName,null);
			}
		}
	}
	protected	void	addWaitPage(int width, int height)
	{
		if (this.movie != null)
		{
			moviePanel.remove(movie);
			movie = null;
		}
		if (this.waitPageFrame == null)
		{
			
			String temp = ConferenceGlobals.defaultUrl; 
			if(null != temp && temp.length() > 0)
			{
				waitPageUrl = temp;
			}
			this.waitPageFrame = new Frame();
			this.waitPageFrame.setStyleName("ppt-broadcaster-frame");
			this.waitPageFrame.setSize(width+"px",height+"px");
			this.moviePanel.add(this.waitPageFrame);
			this.waitPageFrame.setUrl(waitPageUrl);
		}
		else
		{
			this.waitPageFrame.setSize(width+"px",height+"px");
		}
	}
	protected	String	getDTPMovieUrl(UIStreamControlEvent event)
	{
		String	url = "";
		String streamName = event.getStreamName();
		String dtpProtocol = UIGlobals.getStreamingUrlsTable().getDtpProtocol();
		String dtpHost = UIGlobals.getStreamingUrlsTable().getDtpHost();
		String dtpPort = UIGlobals.getStreamingUrlsTable().getDtpPort();
		String dtpShareApp = UIGlobals.getStreamingUrlsTable().getDtpShareApp();
		
		url = dtpProtocol+"://"+dtpHost+":"+dtpPort+"/screenshareviewer/"+
				dtpProtocol+"/"+dtpHost+"/"+dtpPort+"/"+dtpShareApp+"/"+
				ConferenceGlobals.conferenceKey+"/"+streamName+"/"+System.currentTimeMillis();
		DebugPanel.getDebugPanel().addDebugMessage("Dtp viewer url:"+url);
		
		return	url;
		/*
	    	//Window.alert("event = "+event);
		String streamName = event.getStreamName();
		String rtmpUrl = UIGlobals.getStreamingUrlsTable().getDtpRtmpUrl();
//		String rtmptUrl = UIGlobals.getStreamingUrlsTable().getDtpRtmptUrl();
		String movieName = UIGlobals.getPlaceholderMovieUrl();
		String movieUrl = movieName+"?"+rtmpUrl+"$"+streamName;
		
		if (event.getStreamType().equals(UIConstants.RESOURCE_TYPE_DESKTOP) ||
				event.getStreamType().equals(UIConstants.RESOURCE_TYPE_APP_SHARE))
		{
			movieName = "swf/fvnc.swf";
			if (event.getStreamType().equals(UIConstants.RESOURCE_TYPE_DESKTOP))
			{
				this.setHeaderText(true,null,null);
			}
			else
			{
				String resourceName = this.getResourceName(event.getResourceId());
				this.setHeaderText(false,resourceName,null);
			}
			StringBuffer buf = new StringBuffer(movieName);
			
			buf.append("?url=");
			buf.append(rtmpUrl);
			buf.append("&confKey=");
			buf.append(ConferenceGlobals.conferenceKey);
			buf.append("&streamName=");
			buf.append(streamName);
			movieUrl =	buf.toString();
		}
		else
		{
			String resourceName = this.getResourceName(event.getResourceId());
			this.setHeaderText(false,null,resourceName);
		}
		return	movieUrl;
		*--/
	}
	public	void	resizeWidget(int width, int height)
	{
//		Window.alert("Resizing resource player -"+width+"--"+height);
		this.lastKnownWidth = width;
		this.lastKnownHeight = height;
		this.moviePanel.setSize(width+"px", height+"px");
		this.scroller.setSize(width+"px",height+"px");
		if (!this.shareMovie)
		{
//			Window.alert("Resizing wait movie");
			addWaitPage(width,height);
		}
		else if (this.pptFrame != null && this.frameActive)
		{
//		//	Window.alert("Resizing ppt frame");
			setFrameSize(width,height);
			refitPptMovieToFrame();
		}
		else if (this.dtpFrame != null)
		{
			setFrameSize(width,height);
		}
		else// if (this.pptFrame != null && this.frameActive)
		{
//        		if(movie != null){
//        		    moviePanel.remove(movie);
//       		    addMovie(movieUrl);
//        		}
		}
	}
	protected	void	refitPptMovieToFrame()
	{
//		Window.alert("Refitting player to the new frame size");
		showPPTPlayerFrame(this.pptControlEvent, false, true);
	}
	protected	void	setFrameSize(int panelWidth, int panelHeight)
	{
//		Window.alert("Setting resource player frame size:"+panelWidth+"--"+panelHeight);
//		if (this.isInPopout())
//		{
//			this.pptMaxHeight = this.pptMaxHeightLarge;
//			this.pptMaxWidth = this.pptMaxWidthLarge;
//		}
//		boolean fitImageToFrame = false;
		this.pptFrameWidth = panelWidth;
		this.pptFrameHeight = panelHeight;
		if (this.pptFrame != null)
		{
			this.pptFrame.setSize(this.pptFrameWidth+"px",this.pptFrameHeight+"px");
		}
		else if (this.dtpFrame != null)
		{
			this.dtpFrame.setSize(this.pptFrameWidth+"px",this.pptFrameHeight+"px");
		}
		
//		if (this.pptFrameWidth < this.pptMaxWidth || this.pptFrameHeight < this.pptMaxHeight)
//		{
//			this.pptFrameWidth = this.pptMaxWidth;
//			this.pptFrameHeight = this.pptMaxHeight;
//			fitImageToFrame = false;
//		}
//		if (fitImageToFrame)
//		{
//			double currentRatio = this.pptFrameWidth / this.pptFrameHeight;
//			if (currentRatio < this.pptAspectRatio)
//			{
//				//	This means that the height is higher for the available
//				//	width. Let the width remain maximum and scale the
//				//	height;
//				this.pptFrameHeight = (int)(this.pptFrameWidth / this.pptAspectRatio);
//			}
//			else
//			{
//				//	This means that the width is longer for the available
//				//	height. Let the height remain maximum and scale the
//				//	width.
//				this.pptFrameWidth = (int)(this.pptFrameHeight * this.pptAspectRatio);
//			}
//		}
		
	}
	
	/**
	 * PPT Handling..
	 * 
	 * @param event
	 * @return
	 *--/
	public void slideChanged(UIPresentationControlEvent event)
	{
		DebugPanel.getDebugPanel().addDebugMessage("ResourceSharingPlayer:slide::"+event.getSlide());
		if (UIGlobals.isInLobby(this.me))
		{
			return;
		}
		if (!this.shareMovie)
		{
		//	This brute force reloading of the frame for each slide is a sad workaround
		//	for the firefox problem of not being able to call methods into the ppt frame.
		//	A search for the real fix for the problem is in progress.
			DebugPanel.getDebugPanel().addDebugMessage("ResourceSharingPlayer:slide::presentation was not started");
			this.startPresentation(event);
		}
		else if (this.sendMessageToPPTIFrame())
		{
			int currentSlide = event.getSlide().intValue();
			this.slideChangedJS("tabContentFrame",currentSlide+"");
//			this.showSlideChanged(currentSlide);
		}
		if (this.pptControlEvent != null)
		{
			this.pptControlEvent.setSlide(event.getSlide());
			this.pptControlEvent.setShowSlide(event.getSlide());
		}
	}
	private native void slideChangedJS(String frameId,String slideIndex) /*-{
	$wnd.sendSlideChangedToFrame(frameId,slideIndex);
	}-*--/;
	public void annotationsDisabled(UIPresentationControlEvent event)
	{
		if (UIGlobals.isInLobby(this.me))
		{
			return;
		}
	}
	public void annotationsEnabled(UIPresentationControlEvent event)
	{
		if (UIGlobals.isInLobby(this.me))
		{
			return;
		}
		if (this.shareMovie && this.sendMessageToPPTIFrame())
		{
			this.annotation = UIPresentationControlEvent.ANNOTATION_ON;
			this.pptControlEvent.setAnnotation(this.annotation);
			DebugPanel.getDebugPanel().addDebugMessage("ResourceSharingPlayer:slide::enabling annotation");
			this.annotationMessageJS("tabContentFrame",this.annotation);
		}
	}
	private native void annotationMessageJS(String frameId,String msg) /*-{
		$wnd.sendAnnotationMessageToFrame(frameId,msg);
	}-*--/;
	public void startPresentation(UIPresentationControlEvent event)
	{
		if (this.pptControlEvent == null)
		{
			pptControlEvent = event;
			if (this.frameActive)
			{
				this.showPPTPlayerFrame(event,false,false);
				
				this.localEventsModel.pptPresentationStarted(this.lastKnownWidth,
							this.pptMaxWidth,this.lastKnownHeight,this.pptMaxHeight);
			}
		}
	}
	public void onTabChange(boolean active)
	{
		this.frameActive = active;
		if (frameActive)
		{
			if (this.pptFrame != null && this.pptControlEvent != null)
			{
				this.showPPTPlayerFrame(this.pptControlEvent,true,false);
			}
			else if (this.pptControlEvent != null && this.pptFrame == null)
			{
				this.showPPTPlayerFrame(this.pptControlEvent, false, false);
			}
		}
	}
	private	boolean	sendMessageToPPTIFrame()
	{
		if (this.frameActive && ((!this.consolePanelPoppedOut && this.inConsole) || this.inPopout))
		{
			return	true;
		}
		return	false;
	}
	private void showPPTPlayerFrame(UIPresentationControlEvent event,boolean onTabChange,boolean onResize)
	{
		if (UIGlobals.isInLobby(this.me))
		{
			return;
		}
		String movieUrl = this.getPPTMovieUrl(event);
		if (movieUrl != null)
		{
			this.shareMovie = true;
			if (this.movie != null)
			{
				moviePanel.remove(movie);
				movie = null;
			}
			if (this.waitPageFrame != null)
			{
				moviePanel.remove(this.waitPageFrame);
				this.waitPageFrame = null;
			}
			if (this.pptFrame == null)
			{
				this.pptFrame = new Frame();
			}
	//		Window.alert(movieUrl);
			if (!onTabChange && !onResize)
			{
				this.setFrameSize(this.lastKnownWidth,this.lastKnownHeight);
				this.pptFrame.setStyleName("ppt-broadcaster-frame");
				this.scroller.remove(moviePanel);
				this.scroller.add(this.pptFrame);
				DOM.setAttribute(pptFrame.getElement(),"id","tabContentFrame");
				DOM.setAttribute(pptFrame.getElement(),"name","tabContentFrame");
			}
//			if (onResize)
//			{
//				this.setFrameSize(this.lastKnownWidth,this.lastKnownHeight);
//			}
			String rtmpUrl = "disabled";
			String rtmptUrl = "disabled";
			if (LayoutGlobals.isWhiteboardSupported() && ConferenceGlobals.whiteboardEnabled)
			{
				rtmpUrl = UIGlobals.getStreamingUrlsTable().getWhiteboardRtmpUrl();
				rtmptUrl = UIGlobals.getStreamingUrlsTable().getWhiteboardRtmptUrl();
			}
			String dmsUrl = "http://"+ConferenceGlobals.dmsServerAddress+"/";
			
			String url = "/"+ConferenceGlobals.getWebappName()+
				"/html/ppt/GetPPTModulePage.action?action=play"+
				"&pptName="+pptName+
				"&pptUrl="+dmsUrl+
				"&width="+this.pptFrameWidth+
				"&height="+this.pptFrameHeight+
				"&slideWidth="+this.slideWidth+
				"&slideHeight="+this.slideHeight+
				"&numberOfSlides="+numSlides+
				"&startSlideIndex="+showSlide+
				"&meetingId="+ConferenceGlobals.getConferenceKey()+
				"&pptId="+pptId+
				"&rtmpUrl="+rtmpUrl+
				"&rtmptUrl="+rtmptUrl+
//				"&pptType=preloaded"+
				"&inPopout="+(isInPopout()?1:0)+
				"&annotation="+event.getAnnotation()+
				"&t="+System.currentTimeMillis();
//			Window.alert("Frame url:"+url);
			this.pptFrame.setUrl(url);
			
//			this.addPptSharingControl(false, showSlide, numSlides);
//			movie = new DmFlashWidget2("ppt_player",UIConstants.PPT_PLAYER_MOVIE_ID,"700","495",movieUrl,"white");
//			movie.addStyleName("resource-share-movie-top-margin");
//			moviePanel.add(movie);
//			movie.show();
			
			String resourceName = this.getResourceName(event.getResourceId());
			this.setHeaderText(false,null,resourceName);
		}
		else
		{
//			Window.alert("Failed to start ppt sharing:"+event.getResourceId());
		}
	}
	public void stopPresentation(UIPresentationControlEvent event)
	{
		if (UIGlobals.isInLobby(this.me))
		{
			return;
		}
//		this.removePptSharingControl();
		pptControlEvent = null;
		this.scroller.remove(this.pptFrame);
		this.scroller.add(this.moviePanel);
		this.shareMovie = false;
		this.pptFrame = null;
		addWaitPage(this.lastKnownWidth,this.lastKnownHeight);
		this.setHeaderText(false,null,null);
	}
	protected	String	getPPTMovieUrl(UIPresentationControlEvent event)
	{
//		String confKey = ConferenceGlobals.conferenceKey;
		String resourceId = event.getResourceId();
		UIResourceObject res = ClientModel.getClientModel().
				getResourceModel().findResourceObject(resourceId);
		if (res != null)
		{
			this.pptName = resourceId;
			this.pptId = res.getMediaId();
			this.numSlides = res.getSlideCount().intValue();
			this.showSlide = event.getShowSlide().intValue();
			this.slideWidth = res.getWidth().intValue();
			this.slideHeight = res.getHeight().intValue();
			
//			String altConfKey = ResourceGlobals.getMeetingIdIfDefaultResource(res);
//			if (altConfKey != null)
//			{
//				confKey = altConfKey;
//			}
//			String pptReadingUrl = ConferenceGlobals.webappRoot+"presentations/"+
//				confKey+"/"+event.getPresentationId()+"/";
//			this.pptUrl = pptReadingUrl;
			
			return "swf/presentationPlayer.swf?";//+pptReadingUrl+"$"+
//					res.getSlideCount()+"$"+resourceId+"$"+event.getShowSlide();
		}
		else
		{
//			Window.alert("Resource not found, id:"+resourceId);
		}
		return	null;
	}
	protected	String	getResourceName(String resourceId)
	{
		String name = "";
		UIResourceObject res = ClientModel.getClientModel().
				getResourceModel().findResourceObject(resourceId);
		if (res != null)
		{
			name = res.getResourceName();
		}
		return	name;
	}
	public void onLockWhiteboard(UIWhiteboardControlEvent event)
	{
	}
	public void onUnlockWhiteboard(UIWhiteboardControlEvent event)
	{
	}
	public void onWhiteboardStarted(UIWhiteboardControlEvent event)
	{
		this.pptControlEvent = null;
	}
	public void onWhiteboardStopped(UIWhiteboardControlEvent event)
	{
		this.pptControlEvent = null;
	}
//	protected	void	addPptSharingControl(boolean presenter, int currentSlide, int slideCount)
//	{
//		Vector	v = this.pptSharingModel.getShareControlPanels();
//		int size = v.size();
//		for (int i=0; i<size; i++)
//		{
//			HorizontalPanel pptControlPanel = (HorizontalPanel)v.elementAt(i);
//			if (pptControlPanel != null && pptControlPanel.getWidgetCount() > 0)
//			{
//				pptControlPanel.remove(0);
//			}
//			PptShareControl pptShareControl = new PptShareControl(this,presenter,currentSlide+1,slideCount);
//			pptControlPanel.add(pptShareControl);
//			pptControlPanel.setCellHorizontalAlignment(pptShareControl, HorizontalPanel.ALIGN_CENTER);
//			pptControlPanel.setCellVerticalAlignment(pptShareControl, VerticalPanel.ALIGN_MIDDLE);
//		}
//	}
//	protected	void	showSlideChanged(int currentSlide)
//	{
//		Vector	v = this.pptSharingModel.getShareControlPanels();
//		int size = v.size();
//		for (int i=0; i<size; i++)
//		{
//			HorizontalPanel pptControlPanel = (HorizontalPanel)v.elementAt(i);
//			if (pptControlPanel != null && pptControlPanel.getWidgetCount() > 0)
//			{
//				PptShareControl pptShareControl = (PptShareControl)pptControlPanel.getWidget(0);
//				pptShareControl.setCurrentSlide(currentSlide+1);
//			}
//		}
//	}
//	protected	void	removePptSharingControl()
//	{
//		Vector	v = this.pptSharingModel.getShareControlPanels();
//		int size = v.size();
//		for (int i=0; i<size; i++)
//		{
//			HorizontalPanel pptControlPanel = (HorizontalPanel)v.elementAt(i);
//			if (pptControlPanel != null && pptControlPanel.getWidgetCount() > 0)
//			{
//				pptControlPanel.remove(0);
//			}
//		}
//	}
	/**
	 * Popout support interface.
	 *--/
	
	protected	boolean		inConsole = true;
	protected	boolean		inPopout = false;
	protected	boolean		consolePanelPoppedOut = false;
	
	protected	PopoutPanelProxy	popoutPanelProxy;
	protected	UIPresentationControlEvent	pptControlEvent;
	
	public String getPanelData()
	{
		//	A simple safety precaution.
//		Window.alert("Popout asking for panel data");
		String str = "no_data";
		if (this.inConsole)
		{
//			Window.alert("We are in console");
			if (this.pptControlEvent != null)
			{
				str = this.pptControlEvent.toJson();
//				Window.alert("Sending data: "+str);
			}
			else
			{
//				Window.alert("Now stored event to send");
			}
		}
		else
		{
//			Window.alert("We are in popout");
		}
		return this.encodeBase64(str);
	}
	public String getPanelId()
	{
		return "panel.resourceSharingPlayer";
	}
	public void panelPopedOut()
	{
		this.consolePanelPoppedOut = true;
	}
	public void panelPoppedIn()
	{
		this.consolePanelPoppedOut = false;
		this.popoutPanelProxy = null;
		if (this.frameActive && this.pptControlEvent != null)
		{
//			Window.alert("Poping in console in firefox");
//			if (ConferenceGlobals.isBrowserFirefox())
//			{
				Timer t = new Timer()
				{
					public void run()
					{
//						Window.alert("Resetting the current slide index");
//						slideChanged(pptControlEvent);
						resizeWidget(lastKnownWidth,lastKnownHeight);
					}
				};
				t.schedule(10);
//			}
		}
		else if (this.shareMovie)
		{
			//	Means that the dtp player is active. Force the player to reload
			//	in case it may have disconnected.
			if (this.movie != null)
			{
				moviePanel.remove(movie);
				moviePanel.add(movie);
				movie.show();
			}
		}
	}
	public void readPanelData(String dataText)
	{
		//	A simple safety precaution.
//		Window.alert("In popped out resource player: ");
//		if (this.inPopout)
//		{
			String s = this.decodeBase64(dataText);
//			Window.alert("In popped out resource player: "+s);
			if (!s.equals("no_data"))
			{
//				Window.alert("In popped out resource player: "+s);
				try
				{
					JSONValue jsonObject = JSONParser.parse(s);
					if (jsonObject != null)
					{
//						Window.alert("-"+jsonObject+"-");
						JSONObject jObj = jsonObject.isObject();
						if (jObj != null)
						{
							UIPresentationControlEvent pcd = (UIPresentationControlEvent)jsonReader.readObject(jObj);
							if (pcd != null)
							{
								this.startPresentation(pcd);
							}
						}
					}
				}
				catch(Exception e)
				{
//					Window.alert(e.getMessage());
				}
			}
//		}
	}
	public void setPopoutPanelProxy(PopoutPanelProxy popoutPanelProxy)
	{
		this.popoutPanelProxy = popoutPanelProxy;
	}
	public void panelInConsole()
	{
		this.inConsole = true;
		this.inPopout = false;
	}
	public void panelInPopout()
	{
		this.inConsole = false;
		this.inPopout = true;
	}
	public	boolean	isInConsole()
	{
		return	this.inConsole;
	}
	public	boolean	isInPopout()
	{
		return	this.inPopout;
	}
	public void receiveMessageFromPopout(UIPopoutPanelData msg)
	{
	}
	protected	native	String	encodeBase64(String s) /*-{
		return $wnd.Base64.encode(s);
	}-*--/;
	protected	native	String	decodeBase64(String s) /*-{
		return $wnd.Base64.decode(s);
	}-*--/;
	public String getListenerName()
	{
		return "RESOURCE_SHARING_PLAYER";
	}
	public void handleCallFromJS(String data)
	{
	}
	public void handleCallFromJS2(String data1, String data2)
	{
	}
	public void handleCallFromJS3(String data1, String data2, String data3)
	{
	}
	public boolean isFrameActive()
	{
		return frameActive;
	}
	public void setFrameActive(boolean frameActive)
	{
		this.frameActive = frameActive;
	}
	*/
}

