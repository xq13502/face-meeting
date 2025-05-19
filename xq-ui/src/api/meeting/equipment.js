import request from '@/utils/request'

// 查询会议室设备信息列表
export function listEquipment(query) {
  return request({
    url: '/meeting/equipment/list',
    method: 'get',
    params: query
  })
}

// 用户状态修改
export function changeEquipmentStatus(equipmentId, status) {
  const data = {
    equipmentId,
    status
  }
  return request({
    url: '/meeting/equipment/changeStatus',
    method: 'put',
    data: data
  })
}

// 查询会议室设备信息详细
export function getEquipment(equipmentId) {
  return request({
    url: '/meeting/equipment/' + equipmentId,
    method: 'get'
  })
}

// 新增会议室设备信息
export function addEquipment(data) {
  return request({
    url: '/meeting/equipment',
    method: 'post',
    data: data
  })
}

// 修改会议室设备信息
export function updateEquipment(data) {
  return request({
    url: '/meeting/equipment',
    method: 'put',
    data: data
  })
}

// 删除会议室设备信息
export function delEquipment(equipmentId) {
  return request({
    url: '/meeting/equipment/' + equipmentId,
    method: 'delete'
  })
}
