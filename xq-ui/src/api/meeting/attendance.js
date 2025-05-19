import request from '@/utils/request'

// 查询签到考勤列表
export function listAttendance(query) {
  return request({
    url: '/meeting/attendance/list',
    method: 'get',
    params: query
  })
}

// 查询签到考勤详细
export function getAttendance(id) {
  return request({
    url: '/meeting/attendance/info/' + id,
    method: 'get'
  })
}

// 新增签到考勤
export function addAttendance(data) {
  return request({
    url: '/meeting/attendance',
    method: 'post',
    data: data
  })
}

// 修改签到考勤
export function updateAttendance(data) {
  return request({
    url: '/meeting/attendance',
    method: 'put',
    data: data
  })
}

// 删除签到考勤
export function delAttendance(id) {
  return request({
    url: '/meeting/attendance/' + id,
    method: 'delete'
  })
}
