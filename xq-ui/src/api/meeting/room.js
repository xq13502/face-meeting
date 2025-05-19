import request from '@/utils/request'
import { parseStrEmpty } from "@/utils/ruoyi";
// 查询会议室信息列表
export function listRoom(query) {
  return request({
    url: '/meeting/room/list',
    method: 'get',
    params: query
  })
}


// export function changeRoomStatus(id, status) {
//   const data = {
//     equipmentId,
//     status
//   }
//   return request({
//     url: '/meeting/room/changeStatus',
//     method: 'put',
//     data: data
//   })

// }

// 查询会议室信息详细
export function getRoom(roomId) {
  return request({
    url: '/meeting/room/' + parseStrEmpty(roomId),
    method: 'get'
  })
}

// 新增会议室信息
export function addRoom(data) {
  return request({
    url: '/meeting/room',
    method: 'post',
    data: data
  })
}

// 修改会议室信息
export function updateRoom(data) {
  return request({
    url: '/meeting/room',
    method: 'put',
    data: data
  })
}

// 删除会议室信息
export function delRoom(roomId) {
  return request({
    url: '/meeting/room/' + roomId,
    method: 'delete'
  })
}
