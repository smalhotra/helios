<!DOCTYPE HTML>
<html><head>

	
		<title>Helios Web Console</title>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <script type="text/javascript" src="js/jquery.min.js"></script>
		<script type="text/javascript" src='js/jquery.cookie.js'></script>
		<script type="text/javascript" src='js/jquery.metadata.js'></script>
		<script type="text/javascript" src="js/ui/jquery-ui-1.8.9.custom.min.js"></script>   
		<!-- <script type="text/javascript" src="js/ui/jLayout/jquery.jlayout.min.js"></script> -->
		
		<script type="text/javascript" src="js/ui/jLayout/jquery_002.js"></script>
		<script type="text/javascript" src="js/ui/jLayout/jquery_003.js"></script>
		<script type="text/javascript" src="js/ui/jLayout/jlayout_002.js"></script>
		<script type="text/javascript" src="js/ui/jLayout/jlayout_003.js"></script>
		<script type="text/javascript" src="js/ui/jLayout/jlayout.js"></script>
		<script type="text/javascript" src="js/ui/jLayout/jlayout_004.js"></script>
		<script type="text/javascript" src="js/ui/jLayout/jquery.js"></script>
		
    	<script type="text/javascript" src='js/dynatree/jquery.dynatree.min.js'></script>
    	<script type="text/javascript" src='js/dynatree/helios.dynatree.js'></script>
		<script type="text/javascript" src="js/class.js"></script>
		<script type="text/javascript" src="js/jmx/jmx.js"></script>
		<script type="text/javascript" src="js/heliosClient.js"></script>
		<script type="text/javascript" src="tabs/metrics/metrics.js"></script>
		
		
		<link href="js/ui/extruder/mbExtruder.css" media="all" rel="stylesheet" type="text/css">
		
		<script type="text/javascript" src="js/ui/extruder/jquery.hoverIntent.min.js"></script>
		<script type="text/javascript" src="js/ui/extruder/jquery.mb.flipText.js"></script>
		<script type="text/javascript" src="js/ui/extruder/mbExtruder.js"></script>
		
		
		
		<link type="text/css" href="js/ui/css/smoothness/jquery-ui-1.8.9.custom.css" rel="stylesheet" />
		<link type='text/css' href='js/dynatree/skin/ui.dynatree.css' rel='stylesheet'>	
		<link type="text/css" href="css/helios.css" rel="stylesheet" />
		<link type="text/css" media="screen" href="js/ui/jqGrid/ui.jqgrid.css" rel="stylesheet" />
		
		<script type="text/javascript" src="js/ui/jqGrid/grid.locale-en.js" ></script>
		<script type="text/javascript" src="js/ui/jqGrid/jquery.jqGrid.min.js" ></script>
		<script type="text/javascript" src="js/ui/jqGrid/jquery.fmatter.js" ></script>
		
		
		
		<script type="text/javascript" src="js/main.js"></script>
		<script type="text/javascript">
			jQuery(function($) {
				jQuery.jgrid.no_legacy_api = true;				
				init_ui();
			});
		</script>		
	</head><body style="position: relative;" class="{layout: {type: 'border', resize: false, hgap: 6}} ui-widget">
		<div id="topBar"  class="north" style="padding-bottom: 1px">
				<div style="display: inline-block;"><img src="img/Helios_Symbol_45_30.png"></img></div>
				<h1 style="display: inline-block;"><span class="helioslight">Helios</span> Web Console</h1>
				<div style="display: inline-block; width: 10px;"></div>
				<div class="tool-widget" style="display: inline-block;">Session:&nbsp;<div id="displaySession" style="display: inline-block; width:230px;height:20px;border:1px solid blue;">&nbsp;</div></div>
				<div id="ajaxDataLink" class="tool-widget" style="display: inline-block;"><img id="dataLink" src="img/red-light-16X16.png" style="display: inline-block;"></img></div>
				<div class="tool-widget" style="display: inline-block;">Ajax Activity:&nbsp;<img id="ajaxActivity" src="img/red-light-16X16.png" style="display: inline-block;"></img></div>
				<div class="tool-widget" style="display: inline-block;">Feed Activity:&nbsp;<img id="feedActivity" class="feedActivityAware" src="img/red-light-16X16.png" style="display: inline-block;"></img></div>
				<div class="tool-widget" style="display: inline-block;">&nbsp;<button id="termSessionBtn" style="display: inline-block;">Term Session</button></div>
				<div style="display: inline-block; float: right;"><font size="-1" ><a id="diagnosticsToggle">&nbsp;Diagnostics&nbsp;</a></font></div>
				<div style="display: inline-block; float: right;"><font size="-1" ><a id="controlsToggle">&nbsp;Controls&nbsp;</a></font></div>
				
				 

			<!-- 
			<table><tr>
				<td><div style="display: inline-block;"><img src="img/Helios_Symbol_45_30.png"></img></div></td>
				<td><h1 style="display: inline-block;"><span class="helioslight">Helios</span> Web Console</h1></td>
				<td><font size="-2" ><a id="controlsToggle">&nbsp;Controls&nbsp;</a></font></td>
				<td><font size="-2" ><a id="diagnosticsToggle">&nbsp;Diagnostics&nbsp;</a></font></td>
			</tr></table>
			-->
		</div>
		
		
		<div style="top: 40px; width: 1213px; height: 710px; position: absolute;" class="center ui-tabs ui-widget ui-widget-content ui-corner-all">
			<ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header">
				<li class="ui-state-default ui-corner-top ui-tabs-selected ui-state-active"><a href="tabs/metrics/metrics.jsp">Metric Browser</a></li>
				<li class="ui-state-default ui-corner-top"><a href="#tabs-2">JMX Server</a></li>
				<li class="ui-state-default ui-corner-top"><a href="#tabs-3">Camel Routes</a></li>
			</ul>
			<div class="ui-tabs-panel ui-widget-content ui-corner-bottom ui-tabs-hide" id="tabs-2">
			</div>
			<div class="ui-tabs-panel ui-widget-content ui-corner-bottom ui-tabs-hide" id="tabs-3">
			</div>
		</div>
		<div style="position: absolute; left: 0px; top: 40px; width: 202px; height: 725px;" class="west {layout: {type: 'grid', columns: 1, resize: false}} ui-resizable ui-widget">
			<div style="left: 0px; top: 0px; width: 202px; height: 241.667px; position: absolute;" class="panel">
				<div class="ui-state-default" style="padding: 4px;">Panel 1</div>
				<div class="hasDatepicker" id="datepicker"><div class="ui-datepicker-inline ui-datepicker ui-widget ui-widget-content ui-helper-clearfix ui-corner-all"><div class="ui-datepicker-header ui-widget-header ui-helper-clearfix ui-corner-all"><a class="ui-datepicker-prev ui-corner-all" onclick="DP_jQuery.datepicker._adjustDate('#datepicker', -1, 'M');" title="Prev"><span class="ui-icon ui-icon-circle-triangle-w">Prev</span></a><a class="ui-datepicker-next ui-corner-all" onclick="DP_jQuery.datepicker._adjustDate('#datepicker', +1, 'M');" title="Next"><span class="ui-icon ui-icon-circle-triangle-e">Next</span></a><div class="ui-datepicker-title"><span class="ui-datepicker-month">December</span> <span class="ui-datepicker-year">2011</span></div></div><table class="ui-datepicker-calendar"><thead><tr><th class="ui-datepicker-week-end"><span title="Sunday">Su</span></th><th><span title="Monday">Mo</span></th><th><span title="Tuesday">Tu</span></th><th><span title="Wednesday">We</span></th><th><span title="Thursday">Th</span></th><th><span title="Friday">Fr</span></th><th class="ui-datepicker-week-end"><span title="Saturday">Sa</span></th></tr></thead><tbody><tr><td class=" ui-datepicker-week-end ui-datepicker-other-month ui-datepicker-unselectable ui-state-disabled">&nbsp;</td><td class=" ui-datepicker-other-month ui-datepicker-unselectable ui-state-disabled">&nbsp;</td><td class=" ui-datepicker-other-month ui-datepicker-unselectable ui-state-disabled">&nbsp;</td><td class=" ui-datepicker-other-month ui-datepicker-unselectable ui-state-disabled">&nbsp;</td><td class=" " onclick="DP_jQuery.datepicker._selectDay('#datepicker',11,2011, this);return false;"><a class="ui-state-default" href="#">1</a></td><td class=" " onclick="DP_jQuery.datepicker._selectDay('#datepicker',11,2011, this);return false;"><a class="ui-state-default" href="#">2</a></td><td class=" ui-datepicker-week-end " onclick="DP_jQuery.datepicker._selectDay('#datepicker',11,2011, this);return false;"><a class="ui-state-default" href="#">3</a></td></tr><tr><td class=" ui-datepicker-week-end " onclick="DP_jQuery.datepicker._selectDay('#datepicker',11,2011, this);return false;"><a class="ui-state-default" href="#">4</a></td><td class=" " onclick="DP_jQuery.datepicker._selectDay('#datepicker',11,2011, this);return false;"><a class="ui-state-default" href="#">5</a></td><td class=" " onclick="DP_jQuery.datepicker._selectDay('#datepicker',11,2011, this);return false;"><a class="ui-state-default" href="#">6</a></td><td class=" " onclick="DP_jQuery.datepicker._selectDay('#datepicker',11,2011, this);return false;"><a class="ui-state-default" href="#">7</a></td><td class=" " onclick="DP_jQuery.datepicker._selectDay('#datepicker',11,2011, this);return false;"><a class="ui-state-default" href="#">8</a></td><td class=" " onclick="DP_jQuery.datepicker._selectDay('#datepicker',11,2011, this);return false;"><a class="ui-state-default" href="#">9</a></td><td class=" ui-datepicker-week-end " onclick="DP_jQuery.datepicker._selectDay('#datepicker',11,2011, this);return false;"><a class="ui-state-default" href="#">10</a></td></tr><tr><td class=" ui-datepicker-week-end " onclick="DP_jQuery.datepicker._selectDay('#datepicker',11,2011, this);return false;"><a class="ui-state-default" href="#">11</a></td><td class=" " onclick="DP_jQuery.datepicker._selectDay('#datepicker',11,2011, this);return false;"><a class="ui-state-default" href="#">12</a></td><td class=" ui-datepicker-days-cell-over  ui-datepicker-current-day ui-datepicker-today" onclick="DP_jQuery.datepicker._selectDay('#datepicker',11,2011, this);return false;"><a class="ui-state-default ui-state-highlight ui-state-active ui-state-hover" href="#">13</a></td><td class=" " onclick="DP_jQuery.datepicker._selectDay('#datepicker',11,2011, this);return false;"><a class="ui-state-default" href="#">14</a></td><td class=" " onclick="DP_jQuery.datepicker._selectDay('#datepicker',11,2011, this);return false;"><a class="ui-state-default" href="#">15</a></td><td class=" " onclick="DP_jQuery.datepicker._selectDay('#datepicker',11,2011, this);return false;"><a class="ui-state-default" href="#">16</a></td><td class=" ui-datepicker-week-end " onclick="DP_jQuery.datepicker._selectDay('#datepicker',11,2011, this);return false;"><a class="ui-state-default" href="#">17</a></td></tr><tr><td class=" ui-datepicker-week-end " onclick="DP_jQuery.datepicker._selectDay('#datepicker',11,2011, this);return false;"><a class="ui-state-default" href="#">18</a></td><td class=" " onclick="DP_jQuery.datepicker._selectDay('#datepicker',11,2011, this);return false;"><a class="ui-state-default" href="#">19</a></td><td class=" " onclick="DP_jQuery.datepicker._selectDay('#datepicker',11,2011, this);return false;"><a class="ui-state-default" href="#">20</a></td><td class=" " onclick="DP_jQuery.datepicker._selectDay('#datepicker',11,2011, this);return false;"><a class="ui-state-default" href="#">21</a></td><td class=" " onclick="DP_jQuery.datepicker._selectDay('#datepicker',11,2011, this);return false;"><a class="ui-state-default" href="#">22</a></td><td class=" " onclick="DP_jQuery.datepicker._selectDay('#datepicker',11,2011, this);return false;"><a class="ui-state-default" href="#">23</a></td><td class=" ui-datepicker-week-end " onclick="DP_jQuery.datepicker._selectDay('#datepicker',11,2011, this);return false;"><a class="ui-state-default" href="#">24</a></td></tr><tr><td class=" ui-datepicker-week-end " onclick="DP_jQuery.datepicker._selectDay('#datepicker',11,2011, this);return false;"><a class="ui-state-default" href="#">25</a></td><td class=" " onclick="DP_jQuery.datepicker._selectDay('#datepicker',11,2011, this);return false;"><a class="ui-state-default" href="#">26</a></td><td class=" " onclick="DP_jQuery.datepicker._selectDay('#datepicker',11,2011, this);return false;"><a class="ui-state-default" href="#">27</a></td><td class=" " onclick="DP_jQuery.datepicker._selectDay('#datepicker',11,2011, this);return false;"><a class="ui-state-default" href="#">28</a></td><td class=" " onclick="DP_jQuery.datepicker._selectDay('#datepicker',11,2011, this);return false;"><a class="ui-state-default" href="#">29</a></td><td class=" " onclick="DP_jQuery.datepicker._selectDay('#datepicker',11,2011, this);return false;"><a class="ui-state-default" href="#">30</a></td><td class=" ui-datepicker-week-end " onclick="DP_jQuery.datepicker._selectDay('#datepicker',11,2011, this);return false;"><a class="ui-state-default" href="#">31</a></td></tr></tbody></table></div></div>
			</div>
			<div style="left: 0px; top: 241.667px; width: 202px; height: 241.667px; position: absolute;" class="panel">
				<div class="ui-state-default" style="padding: 4px;">Panel 2</div>
				<div id="eq">
					<span class="ui-slider ui-slider-vertical ui-widget ui-widget-content ui-corner-all"><div style="height: 88%;" class="ui-slider-range ui-slider-range-min ui-widget-header"></div><a style="bottom: 88%;" class="ui-slider-handle ui-state-default ui-corner-all" href="#"></a></span>
					<span class="ui-slider ui-slider-vertical ui-widget ui-widget-content ui-corner-all"><div style="height: 77%;" class="ui-slider-range ui-slider-range-min ui-widget-header"></div><a style="bottom: 77%;" class="ui-slider-handle ui-state-default ui-corner-all" href="#"></a></span>
					<span class="ui-slider ui-slider-vertical ui-widget ui-widget-content ui-corner-all"><div style="height: 55%;" class="ui-slider-range ui-slider-range-min ui-widget-header"></div><a style="bottom: 55%;" class="ui-slider-handle ui-state-default ui-corner-all" href="#"></a></span>
					<span class="ui-slider ui-slider-vertical ui-widget ui-widget-content ui-corner-all"><div style="height: 33%;" class="ui-slider-range ui-slider-range-min ui-widget-header"></div><a style="bottom: 33%;" class="ui-slider-handle ui-state-default ui-corner-all" href="#"></a></span>
					<span class="ui-slider ui-slider-vertical ui-widget ui-widget-content ui-corner-all"><div style="height: 40%;" class="ui-slider-range ui-slider-range-min ui-widget-header"></div><a style="bottom: 40%;" class="ui-slider-handle ui-state-default ui-corner-all" href="#"></a></span>
				</div>
			</div>
			<div style="left: 0px; top: 483.333px; width: 202px; height: 241.667px; position: absolute;" class="panel">
				<div class="ui-state-default" style="padding: 4px;">Panel 3</div>
				<div aria-valuenow="59" aria-valuemax="100" aria-valuemin="0" role="progressbar" class="ui-progressbar ui-widget ui-widget-content ui-corner-all" id="progressbar"><div style="width: 59%;" class="ui-progressbar-value ui-widget-header ui-corner-left"></div></div>
			</div>
		<div style="-moz-user-select: none;" unselectable="on" class="ui-resizable-handle ui-resizable-e"></div></div>
		<div style="left: 1427px; top: 40px; width: 253px; height: 725px; position: absolute;" class="east ui-resizable">
			<div role="tablist" class="ui-accordion ui-widget ui-helper-reset" id="accordion">
			<h3 tabindex="0" aria-expanded="true" role="tab" class="ui-accordion-header ui-helper-reset ui-state-active ui-corner-top"><span class="ui-icon ui-icon-triangle-1-s"></span><a tabindex="-1" href="#">Section 1</a></h3>
			<div role="tabpanel" style="height: 566px; overflow: auto;" class="ui-accordion-content ui-helper-reset ui-widget-content ui-corner-bottom ui-accordion-content-active">
				<!-- 
					========================================================
					Singularity Browser
					========================================================
				 -->
				<div id="singularityTreeValue"></div>
				<div id="singularityTree"></div>
				
				<!-- 
					========================================================
				 -->
				
			</div>
			<h3 tabindex="-1" aria-expanded="false" role="tab" class="ui-accordion-header ui-helper-reset ui-state-default ui-corner-all"><span class="ui-icon ui-icon-triangle-1-e"></span><a tabindex="-1" href="#">Section 2</a></h3>
			<div role="tabpanel" style="height: 566px; overflow: auto; display: none;" class="ui-accordion-content ui-helper-reset ui-widget-content ui-corner-bottom">
			</div>
			<h3 tabindex="-1" aria-expanded="false" role="tab" class="ui-accordion-header ui-helper-reset ui-state-default ui-corner-all"><span class="ui-icon ui-icon-triangle-1-e"></span><a tabindex="-1" href="#">Section 3</a></h3>
			<div role="tabpanel" style="height: 566px; overflow: auto; display: none;" class="ui-accordion-content ui-helper-reset ui-widget-content ui-corner-bottom">
				<ul>
					<li>List item one</li>
					<li>List item two</li>
					<li>List item three</li>
				</ul>
			</div>
			<h3 tabindex="-1" aria-expanded="false" role="tab" class="ui-accordion-header ui-helper-reset ui-state-default ui-corner-all"><span class="ui-icon ui-icon-triangle-1-e"></span><a tabindex="-1" href="#">Section 4</a></h3>
			<div role="tabpanel" style="height: 566px; overflow: auto; display: none;" class="ui-accordion-content ui-helper-reset ui-widget-content ui-corner-bottom">
			</div>
			</div>
		<div style="-moz-user-select: none;" unselectable="on" class="ui-resizable-handle ui-resizable-w"></div></div>
		<div style="left: 0px; top: 765px; width: 1646px; height: 17px; position: absolute;" class="south"></div>
	<div id="ui-datepicker-div" class="ui-datepicker ui-widget ui-widget-content ui-helper-clearfix ui-corner-all ui-helper-hidden-accessible"></div></body></html>