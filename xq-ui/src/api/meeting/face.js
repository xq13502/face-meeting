import request from '@/utils/request'

// 录入人脸
export function registerFace(data) {
    return request({
        url: '/face/registerFace',
        method: 'post',
        data: data
    })
}


export function faceCheck(data) {
    return request({
        url: '/face/faceCheck',
        method: 'post',
        data: data
    })
}