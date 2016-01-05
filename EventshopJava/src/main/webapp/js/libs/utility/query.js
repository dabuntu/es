var redraw, g, renderer;
var filterCount = 1;
var aggrCount = 1;
var grpCount = 1;
var pattrnCount = 1;
var tempoCount = 1;
var scCount = 1;
var tcCount = 1;
var resultCount = 1;
var initial ="Start";


/* only do all this when document has finished loading (needed for RaphaelJS) */
    
    //var width = $(document).width() - 20;
    //var height = $(document).height() - 60;

    var gWidth = 780;
    var gHeight = 400;
    
	
	

	var queryFilter =  '';
	var queryGroup = '';
	var queryAggregation = '';
	var queriesAll = [];
	var startFlag = 1;
	
	var edgeColours = ['#FF0000','#00FF00','#0000FF','#CC66FF','#000000','#227700','#002277'];
	var currentColour = "#FF0000";

	function queryGraphStart(operator,dropdown){
		var querySelectedDS = [];		
		//validation
				    $("#ui-accordion-accordion-panel-0").show();
				    
		    
		    $('#canvas').html('');
			$('#canvas').css('display','block');
		  		  
		//Getting selected values
		$('#'+dropdown+' option:selected').each(function(){
	       	var selectedValue =$(this).val();
	       	querySelectedDS.push(selectedValue);			
		});

		filterSelectedDS = removeDuplicates(querySelectedDS);
		startFlag = (queriesAll.length == 0?1:0);
		var queryFilter =  new queryOp(operator, querySelectedDS);
		queriesAll.push(queryFilter);
		formGraph(queriesAll);
		
	}

	//console.log("After removing duplicates: "+removeDuplicates(inputArray));

	function removeDuplicates(inputArray) {
            var outputArray=new Array();

            if(inputArray.length>0){
                jQuery.each(inputArray, function(index, value) {
                    if(jQuery.inArray(value, outputArray) == -1){
                        outputArray.push(value);
                    }
                });
            }           
            return outputArray;
    }



	function queryOp (opType,opNodes) {
		this.opName = getOpName(opType);
		this.opType = opType;
		this.queryNodes = new getQueryNodes(opNodes);
		this.queryEdges = opNodes;
		//this.nodes = getNode(opNodes);
		this.result = "Q"+resultCount;

		$('#sSource').append('<option value="'+this.result+'">'+this.result+'</option>');
		 $('#sSource').multiSelect('refresh');

		$('#gSource').append('<option value="'+this.result+'">'+this.result+'</option>');
		//$('#m-selectable_1').append('<option value="'+this.result+'">'+this.result+'</option>');
		 $('#gSource').multiSelect('refresh');
		 
		$('#aSource').append('<option value="'+this.result+'">'+this.result+'</option>');
		//$('#m-selectable_2').append('<option value="'+this.result+'">'+this.result+'</option>');
		 $('#aSource').multiSelect('refresh');
		 
		$('#pSource').append('<option value="'+this.result+'">'+this.result+'</option>');
		//$('#m-selectable_3').append('<option value="'+this.result+'">'+this.result+'</option>');
		 $('#pSource').multiSelect('refresh');
		 
		$('#tSource').append('<option value="'+this.result+'">'+this.result+'</option>');
		//$('#m-selectable_4').append('<option value="'+this.result+'">'+this.result+'</option>');
		 $('#tSource').multiSelect('refresh');
		 
		$('#spaceCharSource').append('<option value="'+this.result+'">'+this.result+'</option>');
		//$('#m-selectable_5').append('<option value="'+this.result+'">'+this.result+'</option>');
		 $('#spaceCharSource').multiSelect('refresh');
		 
		$('#tempCharSource').append('<option value="'+this.result+'">'+this.result+'</option>');
		//$('#m-selectable_6').append('<option value="'+this.result+'">'+this.result+'</option>');
		 $('#tempCharSource').multiSelect('refresh');
		resultCount++;
		
	}
	
	function getOpName(opType) {
		opName = "";
		switch(opType) {
			case "filter" :
				opName = "F" + filterCount;
				filterCount++;
				break;
			case "group" :
				opName = "G" + grpCount;
				grpCount++;
				break;
			case "aggr" :
				opName = "A" + aggrCount;
				aggrCount++;
				break;			
			case "pattrn" :
				opName = "P" + pattrnCount;
				pattrnCount++;
				break;			
			case "temporal" :
				opName = "T" + tempoCount;
				tempoCount++;
				break;
			case "spaceChar" :
				opName = "SC" + scCount;
				scCount++;
				break;
			case "tempChar" :
				opName = "TC" + tcCount;
				tcCount++;
				break;	
				
				
		}
		return opName;
	}
	
	function getQueryNodes(opNodes) {
		var queryNodes = new Array();
		var count = 0;
		for (var i=0;i<opNodes.length;i++) {	
			/*if(opNodes[i].substring(0,1) == "R") {
				continue;
			}*/
			queryNodes[count] = new queryNode(opNodes[i]);
			//console.log("opnodes: "+opNodes[i]);
			count++;
		}
		//console.log("sending nodes "+queryNodes.length);
		return queryNodes;
	}
	
	//create a node with its edges here
	function queryNode(opNode) {					
			this.nodeName = opNode;			
			// setting the start only for the first query
			this.nodeEdge1 = (checkNode(opNode.substring(0,1))?opNode:(startFlag == 1?initial:""));
			//this.nodeEdge1 =((opNode.substring(0,1) == "Q" || opNode.substring(0,1) == "F" || opNode.substring(0,1) == "G" || opNode.substring(0,1) == "A")?opNode:"");
			this.nodeEdge2 = opNode;//((opNode.substring(0,1) == "Q")?"":opNode);					
	}
	
	var renderResult = function(r, n) {
            /* the Raphael set is obligatory, containing all you want to display */
            var set = r.set().push(
                /* custom objects go here */       	
                r.rect(n.point[0]-30, n.point[1]-13, 60, 44).attr({"fill": "#330019", r : "12px", "stroke-width" : n.distance == 0 ? "3px" : "1px" })).push(
                r.text(n.point[0], n.point[1] + 10, (n.label || n.id) + "\n"));
            return set;
        };
		
		function formGraph(queries) {
            g = new Graph();
           //// g.addNode(initial);
            var colCnt = 0;
        	for(var j=0;j<queries.length;j++) {
        		currentColour =  edgeColours[colCnt];
        		g.addNode(queries[j].opName);                                              
                for (var k=0;k<queries[j].queryNodes.length;k++) {
                   g.addNode(queries[j].queryNodes[k].nodeName);
                	if (queries[j].queryNodes[k].nodeEdge1 != "") {     
                				g.addEdge(queries[j].queryNodes[k].nodeEdge1,queries[j].queryNodes[k].nodeEdge2,{weight:9, directed: true,stroke : edgeColours[colCnt]});
                    }
                	g.addEdge(queries[j].queryNodes[k].nodeName,queries[j].opName,{weight:9, directed: true,stroke :  edgeColours[colCnt]});
                 }
            //,{x:cx, y:cy, label : labelName, render : render}                
                g.addNode(queries[j].result,{render:renderResult});                
                g.addEdge(queries[j].opName,queries[j].result,{weight:9, directed: true,stroke :  edgeColours[colCnt]});                
                colCnt++;              
            }
	

		var layouter = new Graph.Layout.Spring(g);
	    /* draw the graph using the RaphaelJS draw implementation */
			renderer = new Graph.Renderer.Raphael('canvas', g, gWidth, gHeight);			
			redraw = function() {				
				layouter.layout();
				renderer.draw();
			};			
	}
		
		function checkNode(opType) {
			opName = false;
			switch(opType) {
				case "F" :
					opName=true;
					break;
				case "G" :
					opName=true;
					break;
				case "A" :
					opName=true;
					break;			
				case "P" :
					opName=true;
					break;			
				case "T" :
					opName=true;
					break;
				case "SC" :
					opName=true;
					break;
				case "TC" :
					opName=true;
					break;
				case "Q" :
					opName=true;
					break;	
					
					
			}
			return opName;
		}

		//validateAddDataSource:function()
		
