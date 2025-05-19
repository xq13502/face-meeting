import request from '@/utils/request'

// 查询审批记录列表
export function listLog(query) {
  return request({
    url: '/meeting/approval/list',
    method: 'get',
    params: query
  })
}

// 查询审批记录详细
export function getLog(logId) {
  return request({
    url: '/meeting/approval/' + logId,
    method: 'get'
  })
}

// 新增审批记录
export function addLog(data) {
  return request({
    url: '/meeting/approval',
    method: 'post',
    data: data
  })
}

// 修改审批记录
export function updateLog(data) {
  return request({
    url: '/meeting/approval',
    method: 'put',
    data: data
  })
}

// 删除审批记录
export function delLog(logId) {
  return request({
    url: '/meeting/approval/' + logId,
    method: 'delete'
  })
}
