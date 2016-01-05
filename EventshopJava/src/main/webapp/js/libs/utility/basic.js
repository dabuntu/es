function getDataFormatInfo(value)
{
	$('#simplemodal-container').css('height', 'auto');
	//$('#simplemodal-container').css('width', '450px');
	//alert(value);
	
	if(value=="stream")
	{
		//$("#dataFormatData").html($("#data").html());
		$("#rest").css("display", "none");
		$("#file").css("display", "none");
		$("#stream").css("display", "block");
		
		
	}
	else if(value=="rest")
	{
		
		var visualimageArr = [];
		$("#stream").css("display", "none");
		$("#file").css("display", "none");
		$("#rest").css("display", "block");
		$("#addTMPData,#addCMPData,#addMPData").css("display","inline");
		$("#addTMPData,#addCMPData,#addMPData").css("float","left");
		$("#viewTMPData").css("display","block");
		//$("#dataFormatData").html($("#visual").html());
	}
	else if(value=="file")
	{
		$("#stream").css("display", "none");
		$("#rest").css("display", "none");
		$("#file").css("display", "block");
		
		//$("#dataFormatData").html($("#csv").html());
	}
	else
	{
		$("#stream").css("display", "none");
		$("#rest").css("display", "none");
		$("#file").css("display", "none");
	}
}

function getAlertTypeInfo(value) {
	$('#simplemodal-container-alert').css('height', 'auto');
	if(value=="WithSoln")
	{
		$("#solnData").css("display", "block");
	} else {
		$("#solnData").css("display", "none");
	}
}



/*function ViewDataFormatInfo(value)
{
	$('#simplemodal-container').css('height', 'auto');
	//$('#simplemodal-container').css('width', '450px');
	//alert(value);

	if(value=="stream")
	{
		$("#viewDSDataFormat").html($("#viewData").html());
	}
	else if(value=="visual")
	{
		$("#viewDSDataFormat").html($("#viewVisual").html());
	}
	else if(value=="csv")
	{
		$("#viewDSDataFormat").html($("#viewCsv").html());
	}
	else
	{
		$("#viewDSDataFormat").html("");
	}
}*/
jQuery(function ($){
$(document).ready(function () {
	
	/*$("#imgaddDS").click(function(){		
		
		$('#basic-modal-content').modal('show');		
		$('#basic-modal-content').css('height', 'auto');
		$('#basic-modal-content').css('width', 'auto');
		$("#DSTabs").tabs();
		//$('#basic-modal-content').css("display","block");
			
	});*/
	
	$("#viewDSimg").click(function(){
		$('#basic-modal-content').css('height', 'auto');
		//$('#basic-modal-content').css("display","block");
		$( "#DSTabs" ).tabs();	
	});
	$(document).on("click", "#datasourceTable tr ", function(event){
		// console.log(this.id);
		$('#basic-modal-content').css('height', 'auto');
		//$('#basic-modal-content').css("display","block");
		$( "#DSTabs" ).tabs();		
	});
	//$("#viewTMP").click(function(){
	
	$(document).on("click", "#viewTMP", function(event){		
		$('#tmpData').css("display","inline");
	}); 
	
});
});


	
jQuery(function ($) {
	
	// Close dialog on click
	$('#Ok').click(function (e) {
		$.modal.close();

		return false;
	});
});

jQuery(function ($) {
	

	// Close dialog on click
	$('#Ok').click(function (e) {
		$.modal.close();

		return false;
	});
});

jQuery(function ($) {
	
	// Close dialog on click
	$('#add').click(function (e) {
		$.modal.close();

		return false;
	});
});

jQuery(function ($) {
	
	// Close dialog on click
	$('#cancel').click(function (e) {
		$.modal.close();

		return false;
	});
});


jQuery(function ($) {
// Highlight row 
$('#dataTable tr').click(function () {
    $(this).find('td input:radio').prop('checked', true);
    $('#dataTable tr').removeClass("active");
    $(this).addClass("active");
});

});
 jQuery(function ($) {
 $("input[name$='one']").click(function() {
      var test = $(this).val();
	  $("#displayText").val(test);
    });
});

jQuery(function ($) {
// Highlight row 
$('#queryTable tr').click(function () {
    $(this).find('td input:radio').prop('checked', true);
    $('#queryTable tr').removeClass("active");
    $(this).addClass("active");
});

});

jQuery(function ($) {
// Add row on click
$('#add').click(function () {
    var original = $('#firstRow');
	original.clone().appendTo('#datasourceTable');
     $('#datasourceTable tr').removeClass("active");
    $(this).addClass("active");
});
});

/*jQuery(function ($) {
 $("#pagination").pagination({
        items: 100,
        itemsOnPage: 10,
        cssStyle: 'light-theme'
    });
});
*/

jQuery(function ($) {
 $('#maphide').change(function(){
 
       if ($(this).is(":checked")) {
	       $('#googleMap').hide();
       } else {
           $('#googleMap').show();
       }
   });
});


jQuery(function ($) {
 $('#chksptbounds').change(function(){
 
       if ($(this).is(":checked")) {
	       $('#spatBounds').show();
       } else {
           $('#spatBounds').hide();
       }
   });
});


 

function getMaxObjectValue(this_array, element) {
	var values = [];
	for (var i = 0; i < this_array.length; i++) {
			values.push(Math.ceil(parseFloat(this_array[i][""+element])));
	}
	values.sort(function(a,b){return a-b});
	return values[values.length-1];
}

function getMinObjectValue(this_array, element) {
	var values = [];
	for (var i = 0; i < this_array.length; i++) {
			values.push(Math.floor(parseFloat(this_array[i][""+element])));
	}
	values.sort(function(a,b){return a-b});
	return values[0];
}



function filter (term, _id, cellNr){
	var suche = term.value.toLowerCase();
	var table = document.getElementById(_id);
	var ele;
	for (var r = 1; r < table.rows.length; r++){
		ele = table.rows[r].cells[cellNr].innerHTML.replace(/<[^>]+>/g,"");
		if (ele.toLowerCase().indexOf(suche)>=0 )
			table.rows[r].style.display = '';
		else table.rows[r].style.display = 'none';
	}
}

function validation (){

var userId=document.getElementById('userId');
var mailId=document.getElementById('userMailId');
var userPWD=document.getElementById('userPWD');
var fullName=document.getElementById('fullName');
mailVal=mailId.value;
atpos = mailVal.indexOf("@");
dotpos = mailVal.lastIndexOf(".");
if(userId.value==''){
	 alert( "Please provide user name!" );
     return false;
}

if(mailVal=='' ||atpos < 1 || ( dotpos - atpos < 2 )){
 alert( "Please provide valid mail id!" );
     return false;
	 
}

if(userPWD.value==''){
 alert( "Please provide the password!" );
     return false;
}

if(fullName.value==''){
 alert( "Please provide the fullname!" );
     return false;
}

if( userId.value!=='' && mailVal!=='' && userPWD.value!=='' && fullName.value!=='' ){
alert('User created successfully');
window.location.href="login.html";
return true;
}

}
function setCookie(c_name,value,exdays)
{
	var exdate=new Date();
	exdate.setDate(exdate.getDate() + exdays);
	var c_value=escape(value) + ((exdays==null) ? "" : "; expires="+exdate.toUTCString());
	document.cookie=c_name + "=" + c_value;
}

function getCookie(c_name)
{
	var c_value = document.cookie;
	var c_start = c_value.indexOf(" " + c_name + "=");
	if (c_start == -1)
	{
	c_start = c_value.indexOf(c_name + "=");
	}
	if (c_start == -1)
	{
	c_value = null;
	}
	else
	{
	c_start = c_value.indexOf("=", c_start) + 1;
	var c_end = c_value.indexOf(";", c_start);
	if (c_end == -1)
	{
	c_end = c_value.length;
	}
	c_value = unescape(c_value.substring(c_start,c_end));
	}
	return c_value;
}

function logout(){
	
	var checkAdmin=getCookie("checkAdmin");
	setCookie("userName",'',-1);
	setCookie("id",'',-1);
	setCookie("roleId",'',-1);
	
	setCookie("checkAdmin",'',-1);
	if(checkAdmin==1){
		window.location.href="eventshoplinux/#admin";
		location.reload(); 
		
	}else{
		
	  window.location.href="/eventshoplinux/";
	}
}

function loginValidation(){
var loginId=document.getElementById('loginId');
var loginPWD=document.getElementById('loginPWD');
var loginVal=loginId.value;
atpos = loginVal.indexOf("@");
dotpos = loginVal.lastIndexOf(".");


if(loginVal=='' ||atpos < 1 || ( dotpos - atpos < 2 )){
 alert( "Please provide valid mail id!" );
     return false;
	 
}

if(loginPWD.value==''){
 alert( "Please provide the password!" );
     return false;
}

if(loginVal!=='' && loginPWD.value!==''){
window.location.href="index.html";
return true;
}
}

/************* QUery Graph Codes Starts Here ****************/
 // Variable Declaration
 var datasourceList = new Array();
 var resultArray = new Array();
 var selectedDSArray = new Array();
 var tmpGrpValue="";
 var tmpAggValue="";


//Common Functions for Generating Query Graph
//This function is used to whether the element is available in a array 
Array.prototype.contains = function(obj) {
    var i = this.length;
    while (i--) {
        if (this[i] == obj) {
            return true;
        }
    }
    return false;
}

//This function is used to whether the element is available in a array for IE8
if(!Array.prototype.indexOf) {
    Array.prototype.indexOf = function(needle) {
        for(var i = 0; i < this.length; i++) {
            if(this[i] === needle) {
                return i;
            }
        }
        return -1;
    };
}

//This function is used to Find and Remove json array object the element 
function removeDataSource(array, property, value) {
   $.each(array, function(index, result) {
      if(result[property] == value) {
          //Remove from array
          array.splice(index, 1);
      }    
   });
}

function RemoveResultData(resArray,data)
{
	var deleteIndex = resArray.indexOf(data);
	if(deleteIndex != -1)
	{
		resArray.splice(deleteIndex, 1);	
	}
	return resArray
}


