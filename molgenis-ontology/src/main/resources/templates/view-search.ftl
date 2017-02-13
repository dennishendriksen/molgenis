<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[]>
<#assign js=["vue.js", "vue-resource.min.js"]>

<@header css js/>
<style>
    .borderless td, .borderless th {
        border: none;
    }
</style>

<div id="plugin">
    <search-plugin></search-plugin>
</div>

<script type="text/x-template" id="search-plugin-template">
    <div>
        <div class="row">
            <div class="col-md-12">
                <div class="well well-sm">
                    <div class="row">
                        <div class="col-md-6 col-md-offset-3">
                            <input v-model="query" type="text" class="form-control" placeholder="Search">
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <search-results v-if="aggregates" v-on:selectEntityType="selectEntityType" :query="query"
                        :aggregates="aggregates" :entities="entities"></search-results>
    </div>
</script>

<script type="text/x-template" id="plugin-template">
    <div class="row">
        <div class="col-md-3">
            <div class="well well-sm">
                <aggregation-table v-on:selectEntityType="selectEntityType"
                                   :buckets="aggregates.buckets"></aggregation-table>
            </div>
        </div>
        <div v-if="entities" class="col-md-6">
            <entities-table :entities="entities"></entities-table>
        </div>
    </div>
</script>

<script>
    Vue.component('search-plugin', {
        template: '#search-plugin-template',
        data: function () {
            return {
                query: '',
                aggregates: null,
                entities: null
            }
        },
        watch: {
            // whenever question changes, this function will run
            query: function (newQuery) {
                this.aggregates = null;
                this.entities = null;

                this.$http.post('/plugin/search/aggregate', {query: newQuery}).then(response => {
                    this.aggregates = response.body;
                }, response => {
                    console.log("error", response);
                });
            }
        },
        methods: {
            selectEntityType: function (entityTypeId) {
                console.log('plugin click ' + entityTypeId);

                this.$http.get('/api/v2/' + entityTypeId + '?q=*=q=' + this.query).then(response => {
                    this.entities = response.body;
                }, response => {
                    console.log("error", response);
                });
            }
        }
    });

    Vue.component('search-results', {
        props: ['query', 'aggregates', 'entities'],
        template: '#plugin-template',
        methods: {
            selectEntityType: function (entityTypeId) {
                console.log('search-results click ' + entityTypeId);
                this.$emit('selectEntityType', entityTypeId);
            }
        }
    });

    Vue.component('aggregation-table', {
        props: ['buckets'],
        template: '<div><h4>Data sets matching your query</h4><table class="table table-hover table-condensed borderless"><thead><th>Name</th><th>Count</th></thead><tbody><aggregation-table-row v-on:selectEntityType="selectEntityType" v-for="bucket in buckets" :bucket="bucket"></aggregation-table-row></tbody></table></div>',
        methods: {
            selectEntityType: function (entityTypeId) {
                console.log('aggregation-table click ' + entityTypeId);
                this.$emit('selectEntityType', entityTypeId);
            }
        }
    });

    Vue.component('aggregation-table-row', {
        props: ['bucket'],
        template: '<tr v-on:click="selectEntityType"><td>{{ bucket.label }}</td><td>{{ bucket.count }}</td></tr>',
        methods: {
            selectEntityType: function () {
                console.log('aggregation-table-row click ' + this.bucket.id);
                this.$emit('selectEntityType', this.bucket.id);
            }
        }
    });

    Vue.component('entities-table', {
        props: ['entities'],
        template: '<div><h4>Rows matching your query</h4><table><tbody><entities-table-row v-for="entity in entities.items" :entity="entity"></entities-table-row></tbody></table></div>'
    });

    Vue.component('entities-table-row', {
        props: ['entity'],
        template: '<tr><td>{{entity._href}}</td></tr>'
    });

    new Vue({
        el: '#plugin'
    });
</script>
<@footer/>