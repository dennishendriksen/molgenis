<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[]>
<#assign js=["vue.min.js", "vue-resource.min.js"]>

<@header css js/>
<style>
    .borderless td, .borderless th {
        border: none;
    }
</style>
<div id="plugin">
    <div class="row">
        <div class="col-md-12">
            <input v-model="query" type="text" class="form-control" placeholder="Search">
        </div>
    </div>
    <div class="row">
        <div class="col-md-6">
            <div v-if="searchResponse" id="searchResults">
                <small>{{ searchResponse.total }} results.</small>
                <table class="table table-hover table-condensed borderless">
                    <tbody>
                    <tr is="table-row" v-for="hit in searchResponse.hits" :hit="hit"></tr>
                    </tbody>
                </table>
            </div>
        </div>
        <div class="col-md-6">
            <h1>entity info</h1>
        </div>
    </div>
</div>
<script>
    Vue.component('table-row', {
        props: ['hit'],
        template: '<tr v-on:click="selectEntity"><td>{{ hit.entityTypeLabel }}</td><td>{{ hit.entityId }}</td></tr>'
    })

    new Vue({
        el: '#plugin',
        data: {
            query: '',
            searchResponse: null
        },
        watch: {
            // whenever question changes, this function will run
            query: function (newQuery) {
                this.$http.post('/plugin/search', {query: newQuery}).then(response => {
                    this.searchResponse = response.body;
                }, response => {
                    console.log("error", response);
                });
            }
        },
        methods: {
            selectEntity: function () {

            }
        }
    });
</script>
<@footer/>