import { get, post, put, del, patch, upload } from './request'

// ===== 通用分页类型 =====
export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
}

// ===== 用户相关类型 =====
export interface UserInfo {
  id: string
  nickname: string
  avatar: string
  createdAt: string
}

export interface UserStats {
  collectionCount: number
  saleListCount: number
  wishlistCount: number
  soldCount: number
}

export interface UserMeResponse extends UserInfo {
  stats: UserStats
}

// ===== 首页相关类型 =====
export interface HomeOverview {
  stats: {
    totalCollections: number
    totalSaleLists: number
    totalSoldItems: number
    wishlistCount: number
  }
  recentCollections: RecentCollection[]
  recentSaleLists: RecentSaleList[]
}

export interface RecentCollection {
  id: string
  name: string
  coverImage: string
  workName: string
  characterName: string
  status: string
}

export interface RecentSaleList {
  id: string
  title: string
  totalCount: number
  totalPrice: number
  status: string
  createdAt: string
}

// ===== 收藏相关类型 =====
export interface CollectionSaleInfo {
  forSale: boolean
  salePrice?: number
  defects?: string
  freeShipping?: boolean
  negotiable?: boolean
  bundleRules?: string
}

export interface CollectionItem {
  id: string
  name: string
  images: string[]
  coverImage: string
  category?: string
  workName?: string
  characterName?: string
  purchasePrice?: number
  purchaseDate?: string
  purchaseChannel?: string
  quantity?: number
  status: string
  tags?: string[]
  notes?: string
  saleInfo?: CollectionSaleInfo
  createdAt: string
  updatedAt: string
}

export interface CollectionListParams {
  page?: number
  pageSize?: number
  keyword?: string
  itemType?: string
  status?: string
  workName?: string
  characterName?: string
  sortBy?: string
  sortOrder?: 'asc' | 'desc'
  isForSale?: boolean
}

export interface CollectionCreateParams {
  name: string
  images: string[]
  category?: string
  workName?: string
  characterName?: string
  purchasePrice?: number
  purchaseDate?: string
  purchaseChannel?: string
  quantity?: number
  status?: string
  tags?: string[]
  notes?: string
  saleInfo?: CollectionSaleInfo
}

export interface CollectionUpdateParams {
  name?: string
  images?: string[]
  category?: string
  workName?: string
  characterName?: string
  purchasePrice?: number
  purchaseDate?: string
  purchaseChannel?: string
  quantity?: number
  status?: string
  tags?: string[]
  notes?: string
  saleInfo?: CollectionSaleInfo
}

// ===== 类型别名 =====
export type CollectionQueryParams = CollectionListParams
export type PageResponse<T> = PageResult<T>
export type Pagination = Pick<PageResult<any>, 'page' | 'pageSize' | 'total'>

// ===== 出物清单相关类型 =====
export interface SaleListItem {
  id: string
  title: string
  coverImage: string
  totalCount: number
  totalPrice: number
  status: string
  items?: Array<{
    id: string
    name: string
    image?: string
    price?: number
    quantity?: number
    status?: string
    flawNote?: string
    shippingRule?: string
    bargainRule?: string
    bundleRule?: string
    note?: string
  }>
  collections?: CollectionItem[]
  templateId?: string
  shareImage?: string
  shareUrl?: string
  generatedImage?: string
  shareId?: string
  createdAt: string
  updatedAt: string
}

export interface SaleListParams {
  page?: number
  pageSize?: number
  status?: string
  sortBy?: string
  sortOrder?: 'asc' | 'desc'
}

export interface SaleListCreateParams {
  title: string
  collectionIds: string[]
  templateId?: string
  price?: number
  notes?: string
}

export interface SaleListUpdateParams {
  title?: string
  collectionIds?: string[]
  templateId?: string
  price?: number
  notes?: string
}

export interface SaleGenerateParams {
  templateId: string
  title?: string
  showPrice?: boolean
  showWatermark?: boolean
  backgroundColor?: string
  layout?: string
}

export interface SaleShareStatusUpdate {
  shared: boolean
  platform?: string
}

// ===== 模板相关类型 =====
export interface TemplateItem {
  id: string
  name: string
  preview: string
  category: string
  isRecommended: boolean
  config?: Record<string, any>
}

export interface TemplateListParams {
  page?: number
  pageSize?: number
  category?: string
}

// ===== 分享相关类型 =====
export interface ShareDetail {
  id: string
  title: string
  image: string
  items: CollectionItem[]
  templateId?: string
  createdAt: string
}

// ===== 心愿单相关类型 =====
export interface WishlistItem {
  id: string
  name: string
  description?: string
  expectedPrice?: number
  image?: string
  url?: string
  priority: string
  status: string
  createdAt: string
  updatedAt?: string
}

export interface WishlistListParams {
  page?: number
  pageSize?: number
  keyword?: string
  status?: string
}

export interface WishlistCreateParams {
  name: string
  description?: string
  expectedPrice?: number
  image?: string
  url?: string
  priority?: string
}

export interface WishlistUpdateParams {
  name?: string
  description?: string
  expectedPrice?: number
  image?: string
  url?: string
  priority?: string
  status?: string
}

// ===== 反馈相关类型 =====
export interface FeedbackSubmitParams {
  type: string
  content: string
  images?: string[]
  contact?: string
}

// ===== 事件追踪相关类型 =====
export interface EventTrackParams {
  eventName: string
  properties?: Record<string, any>
}

// ===== 上传相关类型 =====
export interface UploadResult {
  url: string
  width?: number
  height?: number
}

// ===== Auth API =====
export const authApi = {
  /** 微信一键登录 */
  wechatLogin: (data: { code: string; nickname?: string; avatar?: string }) =>
    post<{ token: string; refreshToken: string; user: UserInfo }>('/auth/wechat-login', data, { skipAuth: true }),

  /** 刷新 token */
  refresh: (refreshToken: string) =>
    post<{ token: string; refreshToken: string; expiresIn: number }>('/auth/refresh', { refreshToken }, { skipAuth: true }),
}

// ===== User API =====
export const userApi = {
  /** 获取当前用户信息 */
  getMe: () => get<UserMeResponse>('/user/me'),

  /** 更新当前用户信息 */
  updateMe: (data: { nickname?: string; avatar?: string }) => put<UserMeResponse>('/user/me', data),
}

// ===== Home API =====
export const homeApi = {
  /** 获取首页概览 */
  getOverview: () => get<HomeOverview>('/home/overview'),
}

// ===== Upload API =====
export const uploadApi = {
  /** 上传图片 */
  uploadImage: (filePath: string, scene: 'collection' | 'sale') =>
    upload<UploadResult>('/upload/image', filePath, scene),
}

// ===== Collection API =====
export const collectionApi = {
  /** 获取收藏列表 */
  getList: (params?: CollectionListParams) =>
    get<CollectionItem[]>('/collections', params),

  /** 获取收藏详情 */
  getDetail: (id: string) =>
    get<CollectionItem>(`/collections/${id}`),

  /** 新增收藏 */
  create: (data: CollectionCreateParams) =>
    post<CollectionItem>('/collections', data),

  /** 更新收藏 */
  update: (id: string, data: CollectionUpdateParams) =>
    put<CollectionItem>(`/collections/${id}`, data),

  /** 删除收藏 */
  delete: (id: string) =>
    del<void>(`/collections/${id}`),

  /** 批量更新状态 */
  batchUpdateStatus: (ids: string[], status: string) =>
    patch<void>('/collections/batch-status', { ids, status }),

  /** 批量删除 */
  batchDelete: (ids: string[]) =>
    del<void>('/collections/batch-delete', { data: { ids } }),
}

// ===== SaleList API =====
export const saleListApi = {
  /** 获取出物清单列表 */
  getList: (params?: SaleListParams) =>
    get<SaleListItem[]>('/sale-lists', params),

  /** 获取出物清单详情 */
  getDetail: (id: string) =>
    get<SaleListItem>(`/sale-lists/${id}`),

  /** 创建出物清单 */
  create: (data: SaleListCreateParams) =>
    post<SaleListItem>('/sale-lists', data),

  /** 更新出物清单 */
  update: (id: string, data: SaleListUpdateParams) =>
    put<SaleListItem>(`/sale-lists/${id}`, data),

  /** 删除出物清单 */
  delete: (id: string) =>
    del<void>(`/sale-lists/${id}`),

  /** 生成出物图 */
  generate: (id: string, data: SaleGenerateParams) =>
    post<{ imageUrl: string; shareUrl: string }>(`/sale-lists/${id}/generate`, data),

  /** 复制出物清单 */
  duplicate: (id: string) =>
    post<SaleListItem>(`/sale-lists/${id}/duplicate`),

  /** 更新分享状态 */
  updateShareStatus: (id: string, data: SaleShareStatusUpdate) =>
    patch<void>(`/sale-lists/${id}/share`, data),
}

// ===== Template API =====
export const templateApi = {
  /** 获取模板列表 */
  getList: (params?: TemplateListParams) =>
    get<TemplateItem[]>('/templates', params),

  /** 获取模板详情 */
  getDetail: (id: string) =>
    get<TemplateItem>(`/templates/${id}`),
}

// ===== Share API =====
export const shareApi = {
  /** 获取分享详情（无需登录） */
  getShareDetail: (shareId: string) =>
    get<ShareDetail>(`/share/${shareId}`, undefined, { skipAuth: true }),
}

// ===== Wishlist API =====
export const wishlistApi = {
  /** 获取心愿单列表 */
  getList: (params?: WishlistListParams) =>
    get<WishlistItem[]>('/wishlist', params),

  /** 新增心愿单 */
  create: (data: WishlistCreateParams) =>
    post<WishlistItem>('/wishlist', data),

  /** 更新心愿单 */
  update: (id: string, data: WishlistUpdateParams) =>
    put<WishlistItem>(`/wishlist/${id}`, data),

  /** 删除心愿单 */
  delete: (id: string) =>
    del<void>(`/wishlist/${id}`),

  /** 心愿单转为收藏 */
  convertToCollection: (id: string, data: { name?: string; images?: string[]; category?: string; workName?: string; characterName?: string; purchasePrice?: number }) =>
    post<CollectionItem>(`/wishlist/${id}/convert`, data),
}

// ===== Feedback API =====
export const feedbackApi = {
  /** 提交反馈 */
  submit: (data: FeedbackSubmitParams) =>
    post<void>('/feedback', data),
}

// ===== Event API =====
export const eventApi = {
  /** 埋点追踪 */
  track: (data: EventTrackParams) =>
    post<void>('/events/track', data),
}
