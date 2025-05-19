<template>
    <div class="app-container">
        <el-row :gutter="20">
            <!-- 左侧说明区域 -->
            <el-col :span="8">
                <el-card class="instruction-card">
                    <div slot="header">
                        <span>录入人脸说明</span>
                    </div>
                    <div class="instruction-content">
                        <h4>注意事项：</h4>
                        <ol>
                            <li>请确保光线充足，面部清晰</li>
                            <li>请正对摄像头，保持自然表情</li>
                            <li>请避免佩戴墨镜等遮挡面部的物品</li>
                            <li>录入成功后即可使用人脸识别签到</li>
                        </ol>
                        <div class="status-text" :class="statusClass">
                            {{ statusText }}
                        </div>
                    </div>
                </el-card>
            </el-col>

            <!-- 右侧摄像头区域 -->
            <el-col :span="16">
                <el-card class="camera-card">
                    <div slot="header">
                        <span>录入人脸</span>
                    </div>
                    <div class="camera-content">
                        <!-- 摄像头预览 -->
                        <div class="video-container" v-if="!registered">
                            <video id="video" width="500" height="375" autoplay v-if="cameraActive"></video>
                            <div v-else class="camera-placeholder">
                                <el-button type="primary" @click="initCamera" round>
                                    打开摄像头
                                </el-button>
                            </div>
                            <canvas id="canvas" width="500" height="375" style="display: none;"></canvas>
                        </div>
                        <!-- 录入成功显示 -->
                        <div v-else class="success-container">
                            <i class="el-icon-check"></i>
                            <p>人脸信息录入成功！</p>
                        </div>
                        <!-- 操作按钮 -->
                        <div class="button-container">
                            <el-button type="primary" :disabled="!cameraActive || registered" @click="captureFace"
                                round>
                                录入人脸
                            </el-button>
                            <el-button type="warning" v-if="registered" @click="resetRegistration" round>
                                重新录入
                            </el-button>
                        </div>
                    </div>
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<script>
    import { registerFace } from '@/api/meeting/face'

    export default {
        name: 'FaceRegister',
        data() {
            return {
                cameraActive: false,
                registered: false,
                mediaStreamTrack: null,
                status: 'waiting', // waiting, processing, success, error
                statusMessages: {
                    waiting: '请点击"录入人脸"按钮开始录入',
                    processing: '正在处理人脸信息，请稍候...',
                    success: '人脸信息录入成功！',
                    error: '录入失败，请重试'
                }
            }
        },
        computed: {
            statusText() {
                return this.statusMessages[this.status]
            },
            statusClass() {
                return {
                    'status-waiting': this.status === 'waiting',
                    'status-processing': this.status === 'processing',
                    'status-success': this.status === 'success',
                    'status-error': this.status === 'error'
                }
            }
        },

        beforeDestroy() {
            this.closeCamera()
        },
        methods: {
            // 初始化摄像头
            async initCamera() {
                try {
                    const stream = await navigator.mediaDevices.getUserMedia({
                        video: { width: 500, height: 375 },
                        audio: false
                    })

                    this.cameraActive = true
                    this.$nextTick(() => {
                        const video = document.getElementById('video')
                        this.mediaStreamTrack = stream.getTracks()[0]
                        video.srcObject = stream
                        video.play()
                        this.status = 'waiting'
                    })
                } catch (error) {
                    console.error('摄像头启动失败:', error)
                    this.$message.error('摄像头启动失败，请检查设备权限')
                    this.status = 'error'
                }
            },

            // 关闭摄像头
            closeCamera() {
                if (this.mediaStreamTrack) {
                    this.mediaStreamTrack.stop()
                    this.cameraActive = false
                }
            },

            // 捕获人脸图像
            async captureFace() {
                try {
                    this.status = 'processing'

                    // 捕获视频帧到Canvas
                    const video = document.getElementById('video')
                    const canvas = document.getElementById('canvas')
                    const ctx = canvas.getContext('2d')

                    // 绘制视频帧
                    ctx.drawImage(video, 0, 0, canvas.width, canvas.height)

                    // 获取图像数据
                    const imageData = canvas.toDataURL('image/jpeg')

                    // 调用注册接口
                    const res = await registerFace({ faceImage: imageData })

                    if (res.code === 200) {
                        this.registered = true
                        this.status = 'success'
                        this.$message.success('人脸信息录入成功')
                        this.closeCamera()
                    } else {
                        throw new Error(res.msg || '录入失败')
                    }
                } catch (error) {
                    this.status = 'error'
                    this.$message.error(error.message || '录入人脸失败，请重试')
                }
            },

            // 重置注册状态
            resetRegistration() {
                this.registered = false
                this.status = 'waiting'
                this.initCamera()
            }
        }
    }
</script>

<style scoped>
    .camera-placeholder {
        width: 500px;
        height: 375px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #f5f7fa;
        border-radius: 4px;
    }

    .instruction-card,
    .camera-card {
        height: 100%;
        margin-bottom: 20px;
    }

    .instruction-content {
        padding: 10px;
    }

    .instruction-content h4 {
        margin-bottom: 15px;
        color: #303133;
    }

    .instruction-content ol {
        padding-left: 20px;
        margin-bottom: 20px;
    }

    .instruction-content li {
        margin-bottom: 10px;
        color: #606266;
    }

    .camera-content {
        display: flex;
        flex-direction: column;
        align-items: center;
    }

    .video-container {
        margin-bottom: 20px;
        border: 2px solid #EBEEF5;
        border-radius: 4px;
        overflow: hidden;
    }

    .success-container {
        text-align: center;
        padding: 40px 0;
    }

    .success-container i {
        font-size: 80px;
        color: #67C23A;
        margin-bottom: 20px;
    }

    .success-container p {
        font-size: 18px;
        color: #67C23A;
    }

    .button-container {
        margin-top: 20px;
    }

    .status-text {
        margin-top: 20px;
        padding: 10px;
        border-radius: 4px;
        text-align: center;
    }

    .status-waiting {
        color: #909399;
    }

    .status-processing {
        color: #E6A23C;
    }

    .status-success {
        color: #67C23A;
    }

    .status-error {
        color: #F56C6C;
    }
</style>