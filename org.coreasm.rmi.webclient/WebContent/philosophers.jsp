<html>
<head>
<meta charset="UTF-8" />
<title>WebCoreASM - Philosophers-Table</title>
<script src="//ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.min.js"></script>
<script src="CoreASMInterface.js"></script>
<script type="text/javascript">
	var states = [];
	var agents = [];
	var activeState = -1
	var chopTransform = [];
	var selectedPlate = "undef";
	var selectedStick = null;
	
	chopTransform["chopOwnerc1"] = [];
	chopTransform["chopOwnerc2"] = [];
	chopTransform["chopOwnerc3"] = [];
	chopTransform["chopOwnerc4"] = [];
	chopTransform["chopOwnerc5"] = [];
	chopTransform["chopOwnerc1"]["Albert"] = [0.776939, -0.629576, 0.629576, 0.776939, -84.4309, 392.593];
	chopTransform["chopOwnerc2"]["Albert"] = [0.960127, -0.279564, 0.279564, 0.960127, -262.331, 26.2995];
	chopTransform["chopOwnerc3"]["Albert"] = [0.753199, 0.657793, -0.657793, 0.753199, 484.705, -525.166];
	chopTransform["chopOwnerc4"]["Albert"] = [0.904835, 0.425763, -0.425763, 0.904835, 385.951, -374.751];
	chopTransform["chopOwnerc5"]["Albert"] = [0.758706, 0.651433, -0.651433, 0.758706, 313.644, -218.731];
	chopTransform["chopOwnerc1"]["Herbert"] =[0.988201, 0.153163, -0.153163, 0.988201, 89.4604, -52.2592];
	chopTransform["chopOwnerc2"]["Herbert"] = [0.853716, -0.520739, 0.520739, 0.853716, -93.5168, 344.539];
	chopTransform["chopOwnerc3"]["Herbert"] = [0.872645, -0.488355, 0.488355, 0.872645, -67.6602, 113.946];
	chopTransform["chopOwnerc4"]["Herbert"] = [0.586056, -0.810271, 0.810271, 0.586056, 135.729, 327.099];
	chopTransform["chopOwnerc5"]["Herbert"] = [0.975894, 0.218243, -0.218243, 0.975894, 408.076, -20.9613];
	chopTransform["chopOwnerc1"]["Fredrich"] = [0.995458, 0.0952047, -0.0952047, 0.995458, 80.2802, 265.368];
	chopTransform["chopOwnerc2"]["Fredrich"] = [0.880913, 0.473278, -0.473278, 0.880913, 282.598, -196.488];
	chopTransform["chopOwnerc3"]["Fredrich"] = [0.771437, -0.636306, 0.636306, 0.771437, -149.804, 453.017];
	chopTransform["chopOwnerc4"]["Fredrich"] = [0.97175, -0.236013, 0.236013, 0.97175, 174.934, 147.365];
	chopTransform["chopOwnerc5"]["Fredrich"] = [-0.555526, 0.831499, -0.831499, -0.555526, 1033.21, 369.854];
	chopTransform["chopOwnerc1"]["Sina"] = [-0.757771, -0.65252, 0.65252, -0.757771, 646.044, 1121.26];
	chopTransform["chopOwnerc2"]["Sina"] = [0.94234, 0.334658, -0.334658, 0.94234, -142.944, -53.894];
	chopTransform["chopOwnerc3"]["Sina"] = [0.857133, 0.515095, -0.515095, 0.857133, 270.571, -164.509];
	chopTransform["chopOwnerc4"]["Sina"] = [0.888379, -0.459111, 0.459111, 0.888379, -157.146, 257.596];
	chopTransform["chopOwnerc5"]["Sina"] = [0.987361, -0.158486, 0.158486, 0.987361, -21.4203, 305.508];
	chopTransform["chopOwnerc1"]["Juan"] = [0.969013, -0.24701, 0.24701, 0.969013, -297.219, 246.921];
	chopTransform["chopOwnerc2"]["Juan"] = [0.614054, 0.789264, -0.789264, 0.614054, 245.403, -459.307];
	chopTransform["chopOwnerc3"]["Juan"] = [0.896969, 0.442094, -0.442094, 0.896969, 66.6363, -408.209];
	chopTransform["chopOwnerc4"]["Juan"] = [0.795164, 0.606395, -0.606395, 0.795164, 307.806, -162.438];
	chopTransform["chopOwnerc5"]["Juan"] = [0.903773, -0.428012, 0.428012, 0.903773, -123.47, 214.587];
	chopTransform["chopOwnerc1"]["undef"] = [1,0,0,1,0,0];
	chopTransform["chopOwnerc2"]["undef"] = [1,0,0,1,0,0];
	chopTransform["chopOwnerc3"]["undef"] = [1,0,0,1,0,0];
	chopTransform["chopOwnerc4"]["undef"] = [1,0,0,1,0,0];
	chopTransform["chopOwnerc5"]["undef"] = [1,0,0,1,0,0];
	
	function refreshStateTable(stateNr) {
		if (stateNr < states.length && stateNr > -1) {
			state2Print = states[stateNr];
			var svgElement;
			for (var property in state2Print) {
				if (state2Print.hasOwnProperty(property)) {
	            	svgElement = document.getElementById(property);
	            	if (svgElement != null) {
	            		if (property.substr(0,9) == "chopOwner") {
	            			var matrix = document.getElementById("svg").createSVGMatrix();
	            			matrix.a = chopTransform[property][state2Print[property]][0];
	            			matrix.b = chopTransform[property][state2Print[property]][1];
	            			matrix.c = chopTransform[property][state2Print[property]][2];
	            			matrix.d = chopTransform[property][state2Print[property]][3];
	            			matrix.e = chopTransform[property][state2Print[property]][4];
	            			matrix.f = chopTransform[property][state2Print[property]][5];
	            			svgElement.transform.baseVal.getItem(0).setMatrix(matrix);
	            		} else {
		                	if (state2Print[property] == "true"){
		                  		svgElement.style["text-decoration"] = "underline";
		               		}
		                	else {
		                		svgElement.style["text-decoration"] = "line-through";
		                	}
	            		}
              		}
			    }
			}
		}
	};
	
	function setSelectedStick(stick) {
		selectedStick = stick;
		if (selectedPlate != null && selectedStick != null) {
			changeValue("chopOwner(" + stick + ")", selectedPlate);
			selectedStick = null;
			selectedPlate = null;
		}
	}
	
	function setSelectedPlate(plate) {
		selectedPlate = plate;
		if (selectedPlate != null && selectedStick != null) {
			changeValue("chopOwner(" + stick + ")", selectedPlate);
			selectedStick = null;
			selectedPlate = null;
		}
	}
		
	
	function getUpdatesHandler(data) {
		for (i = 0; i < data.length; i++) {
			var newState = JSON.parse(JSON.stringify(states[states.length-1]));
			var update = data[i];
			var newUpdatesHTML = "<li>";
			var newOutputHTML = "";
			var loc;
			newUpdatesHTML += "<table border=\"1\" style=\"width:100%\">";
			for (j = 0; j < update.length; j++) {
				loc = update[j].location.replace("(", "");
				loc = loc.replace(")", "");
				newState[loc] = update[j].value;
				states[states.length] = newState;
			}			
		}
		if(data.length > 0) {
			refreshStateTable(states.length-1);
		}
		activeState = states.length-1;
	}
	
	function toggleHungre(phil) {
		updateASM("hungry(" + phil + ") := " +  !(states[activeState]['hungry' + phil]==='true'), phil);
	}
	
	function toggleEating(phil) {
		changeValue("eating(" + phil + ")", !(states[activeState]['eating' + phil]==='true'));
	}
	
	function update() {
		if(EngineId != "") {
			$("#engineIdLbl").text("Engine ID:" + EngineId)
			getUpdates(getUpdatesHandler);
		}
	}
	
	$(document).ready(
		function() {
			states[0] = {};
			$.ajaxSetup({
				cache : false
			});			 			 

			var auto_refresh = setInterval(function() {
				update()
			}, 1000);

			update();

			$(".command").click(function() {
				if(EngineId != "") {
					$.post("Control", {
						command : $(this).val(),
						engineId : EngineId
					})
				}
			});
			
			$("#specSubmit").submit(function(e) {
				e.preventDefault();
				getEngine(new FormData(this));		
			});
			
			$("#joinEngine").submit(function(e) {
				e.preventDefault();				
				join(this[0].value);		
			});
		});
</script>
</head>
<body>
	<form id="specSubmit" action="Control" method="post"
		enctype="multipart/form-data">
		<input type="file" name="spec" /> <input type="submit" />
	</form>
	<form id="joinEngine" action="Control" method="post">
		<input type="text" name="engineId" /> <input type="hidden"
			name="command" value="join" /> <input type="submit" />
	</form>
	<p id="engineIdLbl">Engine ID:</p>
	<div>
		<button class="command" value="start" type="button">Start</button>
		<button class="command" value="pause" type="button">Pause</button>
		<button class="command" value="step" type="button">Step</button>
	</div>
	<div>
		<button class="command" value="stop" type="button">Stop</button>
		<button onclick="reset('true')">Reset</button>
	</div>

	<svg id="svg" xmlns="http://www.w3.org/2000/svg" width="100%"
		height="100%" xmlns:svg="http://www.w3.org/2000/svg"
		xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 923 781"
		preserveAspectRatio="xMidYMid meet" zoomAndPan="disable">
	<rect id="svgEditorBackground" x="0" y="0" width="923" height="781"
			style="stroke: none; fill: none;" />
	<g id="undef">
	<circle id="table" cx="488" cy="393"
			style="stroke-width: 1px; vector-effect: non-scaling-stroke;"
			stroke="black" r="261.079" fill="khaki" />
	</g>
	<g id="p_albert" onclick="setSelectedPlate('Albert')">
	  <circle id="ps_albert" cx="487" cy="210"
			style="stroke-width: 1px; vector-effect: non-scaling-stroke;"
			stroke="black" r="41.8688" fill="gray" />
	  <circle id="e20_circle" cx="487" cy="210"
			style="stroke-width: 1px; vector-effect: non-scaling-stroke;"
			stroke="black" r="34.8281" fill="white" />
	</g>
	<g id="p_herbert" onclick="setSelectedPlate('Herbert')">
	  <circle id="ps_herbert" cx="676" cy="332"
			style="stroke-width: 1px; vector-effect: non-scaling-stroke;"
			stroke="black" r="41.8688" fill="gray" />
	  <circle id="e7_circle" cx="676" cy="332"
			style="stroke-width: 1px; vector-effect: non-scaling-stroke;"
			stroke="black" r="34.8281" fill="white" />
	</g>
	<g id="p_fredrich" onclick="setSelectedPlate('Fredrich')">
	  <circle id="ps_fredrich" cx="617" cy="540"
			style="stroke-width: 1px; vector-effect: non-scaling-stroke;"
			stroke="black" r="41.8688" fill="gray" />
	  <circle id="e18_circle" cx="617" cy="540"
			style="stroke-width: 1px; vector-effect: non-scaling-stroke;"
			stroke="black" r="34.8281" fill="white" />
	</g>
	<g id="p_sina" onclick="setSelectedPlate('Sina')">
	  <circle id="ps_sina" cx="358" cy="535"
			style="stroke-width: 1px; vector-effect: non-scaling-stroke;"
			stroke="black" r="41.8688" fill="gray" />
	  <circle id="e5_circle" cx="358" cy="535"
			style="stroke-width: 1px; vector-effect: non-scaling-stroke;"
			stroke="black" r="34.8281" fill="white" />
	</g>
	<g id="p_juan" onclick="setSelectedPlate('Juan')">
	  <circle id="ps_Juan" cx="300" cy="327"
			style="stroke-width: 1px; vector-effect: non-scaling-stroke;"
			stroke="black" r="41.8688" fill="gray" />
	  <circle id="e3_circle" cx="300" cy="327"
			style="stroke-width: 1px; vector-effect: non-scaling-stroke;"
			stroke="black" r="34.8281" fill="white" />
	</g>
   <g id="chopOwnerc1" transform="matrix(1,0,0,1,0,0)" onclick="setSelectedStick('c1')">
	<polygon id="chop1" stroke="black"
			style="stroke-width: 1px; vector-effect: non-scaling-stroke;"
			points="532.923,427.31 533.923,349.31 537.923,428.31" fill="black"
			transform="matrix(-0.747081, -0.664733, 0.664733, -0.747081, 739.968, 900.613)" />
  </g>
  <g id="chopOwnerc2" transform="matrix(1,0,0,1,0,0)" onclick="setSelectedStick('c2')">
	<polygon id="chop2" stroke="black"
			style="stroke-width: 1px; vector-effect: non-scaling-stroke;"
			points="312.809,430.968 313.809,352.968 317.809,431.968" fill="black"
			transform="matrix(0.270092, -0.962834, 0.962834, 0.270092, 190.373, 636.814)" />
  </g>
  <g id="chopOwnerc3" transform="matrix(1,0,0,1,0,0)" onclick="setSelectedStick('c3')">
    <polygon id="chop3" stroke="black"
			style="stroke-width: 1px; vector-effect: non-scaling-stroke;"
			points="-24.3849,299.685 -23.3849,221.685 -19.3849,300.685"
			fill="black"
			transform="matrix(0.999391, 0.0348831, -0.0348831, 0.999391, 516.405, 291.963)" />
  </g>
  <g id="chopOwnerc4" transform="matrix(1,0,0,1,0,0)" onclick="setSelectedStick('c4')">
	<polygon id="chop4" stroke="black"
			style="stroke-width: 1px; vector-effect: non-scaling-stroke;"
			points="-185.29,843.123 -184.29,765.123 -180.29,844.123" fill="black"
			transform="matrix(0.409555, 0.912286, -0.912286, 0.409555, 1121.33, 276.448)" />
  </g>
  <g id="chopOwnerc5" transform="matrix(1,0,0,1,0,0)" onclick="setSelectedStick('c5')">
	<polygon id="chop5" stroke="black"
			style="stroke-width: 1px; vector-effect: non-scaling-stroke;"
			points="191.393,96.0407 192.393,18.0407 196.393,97.0407" fill="black"
			transform="matrix(-0.734005, 0.679144, -0.679144, -0.734005, 554.811, 163.78)" />
  </g>
	<text fill="black" style="font-family: Arial; font-size: 20px;"
			x="433.076" y="38.0442" id="e1_texte">Albert</text>
	<text fill="black" style="font-family: Arial; font-size: 20px;"
			x="771.062" y="260.267" id="e2_texte">Herbert</text>
	<text fill="black" style="font-family: Arial; font-size: 20px;"
			x="750.583" y="518.666" id="e3_texte">Fredrich</text>
	<text fill="black" style="font-family: Arial; font-size: 20px;"
			x="170.274" y="518.666" id="e4_texte">Sina</text>
	<text fill="black" style="font-family: Arial; font-size: 20px;"
			x="136.434" y="260.267" id="e5_texte">Juan</text>
	<text id="hungryJuan" fill="black"
			style="font-family: Arial; font-size: 20px; text-decoration: line-through;"
			x="128" y="292.309" onclick="toggleHungre('Juan')">hungry</text>
	<text id="eatingJuan" fill="black"
			style="font-family: Arial; font-size: 20px; text-decoration: line-through;"
			x="128" y="311.947" onclick="toggleEating('Juan')")>eating</text>
	<text id="hungrySina" fill="black"
			style="font-family: Arial; font-size: 20px; text-decoration: line-through;"
			x="163.142" y="548.641" onclick="toggleHungre('Sina')">hungry</text>
	<text id="eatingSina" fill="black"
			style="font-family: Arial; font-size: 20px; text-decoration: line-through;"
			x="163.142" y="568.279" onclick="toggleEating('Sina')")>eating</text>
	<text id="hungryFredrich" fill="black"
			style="font-family: Arial; font-size: 20px; text-decoration: line-through;"
			x="753.324" y="553.809" onclick="toggleHungre('Fredrich')">hungry</text>
	<text id="eatingFredrich" fill="black"
			style="font-family: Arial; font-size: 20px; text-decoration: line-through;"
			x="753.324" y="573.447" onclick="toggleEating('Fredrich')")>eating</text>
	<text id="hungryHerbert" fill="black"
			style="font-family: Arial; font-size: 20px; text-decoration: line-through;"
			x="775.029" y="295.41" onclick="toggleHungre('Herbert')">hungry</text>
	<text id="eatingHerbert" fill="black"
			style="font-family: Arial; font-size: 20px; text-decoration: line-through;"
			x="775.029" y="315.048" onclick="toggleEating('Herbert')")>eating</text>
	<text id="hungryAlbert" fill="black"
			style="font-family: Arial; font-size: 20px; text-decoration: line-through;"
			x="438.077" y="69.0528" onclick="toggleHungre('Albert')">hungry</text>
	<text id="eatingAlbert" fill="black"
			style="font-family: Arial; font-size: 20px; text-decoration: line-through;"
			x="438.077" y="88.6908" onclick="toggleEating('Albert')")>eating</text>
</svg>
</body>
</html>