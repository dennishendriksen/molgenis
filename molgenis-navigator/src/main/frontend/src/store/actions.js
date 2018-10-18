// @flow
import type { Folder, Item, State } from '../flow.types'
import {
  fetchJob,
  getFolder,
  getItemsByFolderId,
  getItemsByQuery,
  createItem,
  copyItems,
  deleteItems,
  moveItems,
  updateItem
} from '../utils/api.js'
import {
  ADD_ALERTS,
  RESET_CLIPBOARD,
  SET_FOLDER,
  SET_ITEMS,
  SET_JOBS,
  SET_SELECTED_ITEMS
} from './mutations'

export const FETCH_ITEMS = '__FETCH_ITEMS__'
export const FETCH_ITEMS_BY_QUERY = '__FETCH_ITEMS_BY_QUERY__'
export const FETCH_ITEMS_BY_FOLDER = '__FETCH_ITEMS_BY_FOLDER__'
export const SELECT_ITEM = '__SELECT_ITEM__'
export const DESELECT_ITEM = '__DESELECT_ITEM__'
export const SELECT_ALL_ITEMS = '__SELECT_ALL_ITEMS__'
export const DESELECT_ALL_ITEMS = '__DESELECT_ALL_ITEMS__'
export const DELETE_SELECTED_ITEMS = '__DELETE_SELECTED_ITEMS__'
export const CREATE_ITEM = '__CREATE_ITEM__'
export const UPDATE_ITEM = '__UPDATE_ITEM__'
export const MOVE_CLIPBOARD_ITEMS = '__MOVE_CLIPBOARD_ITEMS__'
export const COPY_CLIPBOARD_ITEMS = '__COPY_CLIPBOARD_ITEMS__'
export const SCHEDULE_DOWNLOAD_SELECTED_ITEMS = '__SCHEDULE_DOWNLOAD_SELECTED_ITEMS__'

function pollJob (commit: Function, state: State, jobId: string) {
  fetchJob(jobId).then(updatedJob => {
    const index = state.jobs.findIndex(job => job.type === updatedJob.type && job.id === updatedJob.id)
    let updatedJobs = state.jobs.slice()
    updatedJobs[index] = updatedJob
    commit(SET_JOBS, updatedJobs)

    switch (updatedJob.status) {
      case 'pending':
      case 'running':
        setTimeout(() => pollJob(commit, state, jobId), 500)
        break
      case 'success':
      case 'failed':
      case 'canceled':
        console.log('job finished with status ' + updatedJob.status)
        break
    }
  })
}

export default {
  [FETCH_ITEMS] ({state, dispatch}: { state: State, dispatch: Function }) {
    if (state.query) {
      dispatch(FETCH_ITEMS_BY_QUERY, state.query)
    } else {
      dispatch(FETCH_ITEMS_BY_FOLDER, state.route.params.folderId)
    }
  },
  [FETCH_ITEMS_BY_QUERY] ({commit}: { commit: Function }, query: string) {
    getItemsByQuery(query).then(items => {
      commit(SET_ITEMS, items)
    }).catch(error => {
      commit(ADD_ALERTS, error.alerts)
    })
  },
  [FETCH_ITEMS_BY_FOLDER] ({commit, dispatch}: { commit: Function, dispatch: Function }, folderId: ?string) {
    const folderFetch = getFolder(folderId)
    const itemsFetch = getItemsByFolderId(folderId)
    Promise.all([folderFetch, itemsFetch]).then(responses => {
      commit(SET_FOLDER, responses[0])
      commit(SET_ITEMS, responses[1])
    }).catch(error => {
      commit(ADD_ALERTS, error.alerts)
    })
  },
  [SELECT_ALL_ITEMS] ({commit, state}: { commit: Function, state: State }) {
    commit(SET_SELECTED_ITEMS, state.items.slice())
  },
  [DESELECT_ALL_ITEMS] ({commit}: { commit: Function }) {
    commit(SET_SELECTED_ITEMS, [])
  },
  [SELECT_ITEM] ({commit, state}: { commit: Function, state: State }, item: Item) {
    commit(SET_SELECTED_ITEMS, state.selectedItems.concat(item))
  },
  [DESELECT_ITEM] ({commit, state}: { commit: Function, state: State }, item: Item) {
    commit(SET_SELECTED_ITEMS, state.selectedItems.filter(selectedItem => !(selectedItem.type === item.type && selectedItem.id === item.id)))
  },
  [DELETE_SELECTED_ITEMS] ({commit, state, dispatch}: { commit: Function, state: State, dispatch: Function }) {
    if (state.selectedItems.length > 0) {
      deleteItems(state.selectedItems).then(() => {
        dispatch(FETCH_ITEMS)
      }).catch(error => {
        commit(ADD_ALERTS, error.alerts)
      })
    }
  },
  [CREATE_ITEM] ({commit, dispatch}: { commit: Function, dispatch: Function },
    item: Item) {
    createItem(item).then(() => {
      dispatch(FETCH_ITEMS)
    }).catch(error => {
      commit(ADD_ALERTS, error.alerts)
    })
  },
  [UPDATE_ITEM] ({commit, state, dispatch}: { commit: Function, state: State, dispatch: Function },
    updatedItem: Item) {
    const item = state.items.find(item => item.type === updatedItem.type && item.id === updatedItem.id)
    updateItem(item, updatedItem).then(() => {
      dispatch(FETCH_ITEMS)
    }).catch(error => {
      commit(ADD_ALERTS, error.alerts)
    })
  },
  [MOVE_CLIPBOARD_ITEMS] ({commit, state, dispatch}: { commit: Function, state: State, dispatch: Function },
    folder: Folder) {
    if (state.clipboard && state.clipboard.items.length > 0) {
      moveItems(state.clipboard.items, folder).then(responses => {
        commit(RESET_CLIPBOARD)
        dispatch(FETCH_ITEMS)
      }).catch(error => {
        commit(ADD_ALERTS, error.alerts)
      })
    }
  },
  [COPY_CLIPBOARD_ITEMS] ({commit, state, dispatch}: { commit: Function, state: State, dispatch: Function },
    folder: Folder) {
    if (state.clipboard && state.clipboard.items.length > 0) {
      copyItems(state.clipboard.items, folder).then(responses => {
        commit(RESET_CLIPBOARD)
        dispatch(FETCH_ITEMS)
      }).catch(error => {
        commit(ADD_ALERTS, error.alerts)
      })
    }
  },
  [SCHEDULE_DOWNLOAD_SELECTED_ITEMS] ({commit, state}: { commit: Function, state: State }) {
    const dummyJob = {type: 'download', id: 'aaaacztxsh4nqabegilmlgaaae', status: 'pending'}
    pollJob(commit, state, dummyJob)
    commit(SET_SELECTED_ITEMS, [])
    // 1. Call download endpoint which will return job location
    // 2. Poll job location until end state is reached, show progress alert ...
    // 3. If success: download file
  }
}
