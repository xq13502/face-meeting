import request from '@/utils/request'

// 查询请假列表
export function listLeave(query) {
  return request({
    url: '/meeting/leave/list',
    method: 'get',
    params: query
  })
}

// 查询请假列表
export function getLeaveListById(query) {
  return request({
    url: '/meeting/leave/getLeaveListById',
    method: 'get',
    params: query
  })
}


// 查询请假详细
export function getLeave(id) {
  return request({
    url: '/meeting/leave/' + id,
    method: 'get'
  })
}

// 新增请假
export function addLeave(data) {
  return request({
    url: '/meeting/leave',
    method: 'post',
    data: data
  })
}

// 修改请假
export function updateLeave(data) {
  return request({
    url: '/meeting/leave',
    method: 'put',
    data: data
  })
}


// 删除请假
export function delLeave(id) {
  return request({
    url: '/meeting/leave/' + id,
    method: 'delete'
  })
}
