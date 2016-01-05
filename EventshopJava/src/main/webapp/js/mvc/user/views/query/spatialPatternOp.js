define([
  'jquery',
  'underscore',
  'backbone',
  'text!templates/user/query/spatialPatternOpTpl.html'
 ], function($, _, Backbone,spatialPatternOpTpl){
    var self;
    var spatialPatternOpView = Backbone.View.extend({
    el: $("#tabs-4"),

    initialize: function(opts){
          self = this;
          self.app_router = opts.router;
          this.generatedQueriesArr = opts.generatedQueriesArr;
          this.queryCount = opts.queryCount;
    },
    events :{
        'click #spmatchingQuery':'generateSpmatchingQuery'
    },
    generateSpmatchingQuery: function(e){
         e.preventDefault();
        var spmatchingQuery = new Object();
        spmatchingQuery.qID = this.queryCount;
        spmatchingQuery.patternType = 'spmatching';
        spmatchingQuery.dataSources = [];
        spmatchingQuery.sizeNorm = $('#sp_size_norm option:selected').text();
        spmatchingQuery.valueNorm = $('#sp_value_norm option:selected').text();
        spmatchingQuery.patternSrc = 0;
        spmatchingQuery.filePath = '';
                    
        $('#pSource option:selected').each(function(){
            var selectedValue = $(this).val();
            spmatchingQuery.dataSources.push(selectedValue);            
        });
        
        var ptSrcType = $('#sp_pattern_type option:selected').val();
        //alert("ptSrcType "+ptSrcType);
            if(ptSrcType == '8'){
                spmatchingQuery.patternSrc = 0;
                spmatchingQuery.filePath = $('#spatialPatternInputFile').val();
            }
            if(ptSrcType == '9'){
                spmatchingQuery.patternSrc = 1;
                spmatchingQuery.numRows = $('#sp_num_rows').val();
                spmatchingQuery.numCols = $('#sp_num_cols').val();
                spmatchingQuery.parmType = $('#prtn_gs_ln option:selected').text();
                                    
                if(spmatchingQuery.parmType == 'Gaussian'){
                    
                    spmatchingQuery.gaussParam = {};
                    var gs_center = $('#gs_center').val();
                    var arrSP = gs_center.split(',');                       
                    spmatchingQuery.gaussParam.centerX = arrSP[0];
                    spmatchingQuery.gaussParam.centerY = arrSP[1];
                    
                    var gs_sd = $('#gs_sd').val();
                    var arrSD = gs_sd.split(',');                       
                    spmatchingQuery.gaussParam.varX = arrSD[0];
                    spmatchingQuery.gaussParam.varY = arrSD[1];
                    
                    spmatchingQuery.gaussParam.amplitude = $('#gs_amp').val();
                    
                }
                if(spmatchingQuery.parmType == 'Linear'){
                    
                    spmatchingQuery.linearParam = {};
                    var ln_start_position = $('#ln_start_position').val();
                    var arrSP = ln_start_position.split(',');                       
                    spmatchingQuery.linearParam.startX = arrSP[0];
                    spmatchingQuery.linearParam.startY = arrSP[1];
                    spmatchingQuery.linearParam.startValue = $('#ln_start_value').val();
                    spmatchingQuery.linearParam.dirGradient = $('#ln_direct_gradient').val();
                    spmatchingQuery.linearParam.valGradient = $('#ln_value_gradient').val();                        
                }
            }
                                
        this.generatedQueriesArr.push(spmatchingQuery);
        
        console.log("spmatchingQuery : "+JSON.stringify(spmatchingQuery));
        this.queryCount++;
        
    },
    render: function(){
     this.$el.html(spatialPatternOpTpl);
    }

  });

  return spatialPatternOpView;
  
});
