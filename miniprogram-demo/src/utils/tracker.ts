import { eventApi } from '../services/api'

/** 关键事件枚举 */
export enum TrackEvent {
  LOGIN_SUCCESS = 'login_success',
  COLLECTION_CREATE = 'collection_create',
  COLLECTION_EDIT = 'collection_edit',
  SALE_LIST_CREATE_START = 'sale_list_create_start',
  SALE_LIST_ITEM_SELECTED = 'sale_list_item_selected',
  SALE_LIST_GENERATE = 'sale_list_generate',
  IMAGE_SAVE = 'image_save',
  COPY_TEXT = 'copy_text',
  SHARE_SALE_LIST = 'share_sale_list',
  WISHLIST_CREATE = 'wishlist_create',
  FEEDBACK_SUBMIT = 'feedback_submit',
}

/** 埋点上报 */
export function track(event: string, properties?: Record<string, any>): void {
  eventApi.track({
    eventName: event,
    properties: properties || {},
  }).catch(() => {
    // 埋点失败不影响主流程
  })
}
