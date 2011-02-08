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

package com.dimdim.conference.ui.common.client.list;

import com.google.gwt.user.client.ui.Image;

/**
 * @author Jayant Pandit
 * @email Jayant.Pandit@communiva.com
 * 
 * All the images must be of same size. A difference is not impossible to
 * accomodate, just needs lot of additional methods to support.
 */

public interface ListEntryPropertiesProvider
{
	public	String	getListEntryPanelStyle();
	
	public	String	getListEntryPanelBackgroundStyle();
	
	public	int		getNameLabelWidth();
	
	public	String	getNameLabelStyle();
	
	public	int		getImageWidthPX();
	
	public	int		getImageHeightPX();
	
	public	Image	getImage1Url();
	
	public	String	getImage1Tooltip();
	
	public	Image	getImage2Url();
	
	public	String	getImage2Tooltip();
	
	public	Image	getImage3Url();
	
	public	String	getImage3Tooltip();
	
	public	Image	getImage4Url();
	
	public	String	getImage4Tooltip();
	
	public	Image	getImage5Url();
	
	public	String	getImage5Tooltip();
	
	public	ListEntryMovieModel	getMovie1Model();
	
	public	ListEntryMovieModel	getMovie2Model();
	
}

