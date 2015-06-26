<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=[]>
<#assign js=["rawimporter.js"]>
<@header css js/>
<div class="row">
    <div class="col-md-6 col-md-offset-3">
        <div id="raw-importer-container">
            <div class="row">
                <div class="col-md-12">
                    <form name="raw-importer-analysis-form" class="form" action="${context_url?html}/analyze" method="POST" enctype="multipart/form-data">           
                        <div class="form-group" id="file-input-container"></div>
                            <button type="submit" class="btn btn-default pull-right">Submit</button>
                    </form>
                </div>
            </div>
            <hr>
            <div id="file-analysis-results">
                <div id="file-analysis-results-table-container"></div>
                <form name="raw-importer-import-form" class="form" action="${context_url?html}/analyze" method="POST" enctype="multipart/form-data">           
                    <div class="form-group" id="file-input-container"></div>
                    <button type="submit" class="btn btn-default pull-right">Submit</button>
                </form>                    
            </div>
        </div>
    </div>
</div>
<@footer/>