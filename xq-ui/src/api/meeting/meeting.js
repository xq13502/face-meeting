import request from '@/utils/request'
import { parseStrEmpty } from "@/utils/ruoyi";
// 查询会议记录列表
export function listMeeting(query) {
  return request({
    url: '/meeting/info/list',
    method: 'get',
    params: query
  })
}

export function getUserAllMeeting(query) {
  return request({
    url: '/meeting/info/getUserAllMeeting',
    method: 'get',
    params: query
  })
}


export function checkMeeting(data) {
  return request({
    url: '/meeting/info/checkMeeting',
    method: 'post',
    data: data
  })
}

export function signatureMeeting(data) {
  return request({
    url: '/meeting/info/signatureMeeting',
    method: 'post',
    data: data
  })
}



// 查询会议记录详细
export function getMeeting(meetingId) {
  return request({
    url: '/meeting/info/' + parseStrEmpty(meetingId),
    method: 'get'
  })
}

export function getUserList(meetingId) {
  return request({
    url: '/meeting/info/getUserList/' + meetingId,
    method: 'get'
  })
}

//查询用户未来参与会议
export function getUserFutureMeeting() {
  return request({
    url: '/meeting/info/getUserFutureMeeting',
    method: 'get'
  })
}


// 新增会议记录
export function addMeeting(data) {
  return request({
    url: '/meeting/info',
    method: 'post',
    data: data
  })
}

// 修改会议记录
export function updateMeeting(data) {
  return request({
    url: '/meeting/info',
    method: 'put',
    data: data
  })
}

// 删除会议记录
export function delMeeting(meetingId) {
  return request({
    url: '/meeting/info/' + meetingId,
    method: 'delete'
  })
}

