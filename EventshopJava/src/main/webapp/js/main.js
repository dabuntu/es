// Author: Thomas Davis <thomasalwyndavis@gmail.com>
// Filename: main.js

// Require.js allows us to configure shortcut alias
// Their usage will become more apparent futher along in the tutorial.
require.config({
 
  paths: {
	   	jquery: 'libs/jquery/jquery-1.9.1',	 
	    jqueryui: 'libs/jquery/jquery-ui',
	    underscore: 'libs/underscore/underscore-min',
	    bootMin: 'libs/bootstrap/bootstrap.min',
	    backbone: 'libs/backbone/backbone-min',	   
	    dalgorithms: 'libs/dracula/algorithms',
	    ddalgorithms: 'libs/dracula/dracula_algorithms',
	    dgraffle: 'libs/dracula/dracula_graffle',
	    dgraph: 'libs/dracula/dracula_graph',
	    modal: 'libs/modal/jquery.simplemodal',
	    paginate: 'libs/pagination/paging',
	    sortable: 'libs/sort/sorttable',	    
	    queryGraph: 'libs/utility/query',
	    carousel: 'libs/bootstrap/bootstrap-carousel',
	    moment: 'libs/moment/moment.min',
	    datetimepicker: 'libs/bootstrap/bootstrap-datetimepicker.min',
	    dataTables: 'libs/dataTables/js/jquery.dataTables',
	    multiselectable: 'libs/multiselect/jquery.multi-select',
	    d3: 'libs/map/d3.v3.min',	    
	    topojson: 'libs/map/topojson.v1.min',
	    basic: 'libs/utility/basic',	    
	    templates: '../templates',
	    leaflet: 'libs/map/leaflet',
	    queue: 'libs/map/queue.v1.min'
  },
  shim: {
	  jquery:{
		  exports: '$'
	  },
	  jqueryui:{
		  deps: ['jquery'],
		  exports: 'jqueryui'
	  },
	  bootMin:{
		  exports: 'bootMin'
	  },
	  underscore: {
          exports: '_'
      },
      backbone: {
          //These script dependencies should be loaded before loading backbone.js
          deps: ['underscore', 'jquery'],
          //Once loaded, use the global 'Backbone' as the module value.
          exports: 'Backbone'
      }
  }

});

require([
  // Load our router module and pass it to our definition function
  'router', 	// request router.js

], function(Router){
  // The "router" dependency is passed in as "Router"
  // Again, the other dependencies passed in are not "AMD" therefore don't pass a parameter to this function
	Router.initialize();
});
