<template>
    <div class="sign-container">
        <div class="sign-card">
            <h2>{{ actionType === 'checkIn' ? '会议签到' : '会议签退' }}</h2>
            <p class="meeting-name">{{ meetingName }}</p>

            <!-- 状态容器 -->
            <div class="status-container">
                <div v-if="signStatus === 'success'" class="success-status">
                    <i class="el-icon-check"></i>
                    <p>{{ actionType === 'checkIn' ? '签到成功' : '签退成功' }}</p>
                </div>

                <div v-else-if="signStatus === 'error'" class="error-status">
                    <i class="el-icon-close"></i>
                    <p>{{ errorMessage }}</p>
                    <p class="pending-tip">请尝试重新扫码</p>
                </div>

                <div v-else class="loading-status">
                    <i class="el-icon-loading"></i>
                    <p>正在验证中...</p>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    import { verifyCheckToken, confirmSign } from '@/api/meeting/qrcode';

    export default {
        name: 'SignPage',
        data() {
            return {
                token: null,
                actionType: 'checkIn',
                meetingName: '未知会议',
                signStatus: 'loading', // success/error/loading
                errorMessage: ''
            }
        },
        async created() {
            this.token = this.$route.query.token;
            if (!this.token) {
                this.showError('无效的二维码');
                return;
            }
            await this.autoSign();
        },
        methods: {
            async autoSign() {
                try {
                    // 合并验证与签到步骤
                    const verifyRes = await verifyCheckToken({ token: this.token });

                    if (!verifyRes.data.valid) {
                        this.showError(verifyRes.data.msg || '二维码已失效');
                        return;
                    }

                    // 更新会议信息
                    this.actionType = verifyRes.data.actionType;
                    this.meetingName = verifyRes.data.meetingName;

                    // 自动执行签到
                    const signRes = await confirmSign({
                        token: this.token,
                        userId: this.$store.state.user.id
                    });

                    if (signRes.data.code === 200) {
                        this.signStatus = 'success';
                        setTimeout(() => window.close(), 1500);
                    } else {
                        this.showError(signRes.data.msg);
                    }

                } catch (error) {
                    this.showError('请求失败，请检查网络');
                }
            },
            showError(msg) {
                this.signStatus = 'error';
                this.errorMessage = msg;
            }
        }
    }
</script>

<style scoped>
    .sign-container {
        padding: 20px;
        display: flex;
        justify-content: center;
        min-height: 100vh;
        background: #f5f7fa;
    }

    .sign-card {
        background: #fff;
        padding: 40px;
        border-radius: 12px;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
        text-align: center;
        width: 100%;
        max-width: 400px;
        margin-top: 100px;
    }

    .meeting-name {
        color: #666;
        margin: 15px 0 30px;
        font-size: 16px;
    }

    /* 状态容器 */
    .status-container {
        text-align: center;
        min-height: 200px;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .success-status {
        color: #67C23A;
    }

    .success-status i {
        font-size: 60px;
        margin-bottom: 15px;
        animation: bounceIn 0.75s;
    }

    .error-status {
        color: #F56C6C;
    }

    .error-status i {
        font-size: 60px;
        margin-bottom: 15px;
    }

    .loading-status {
        color: #409EFF;
    }

    .loading-status i {
        font-size: 40px;
        margin-bottom: 15px;
        animation: rotating 2s linear infinite;
    }

    .pending-tip {
        color: #909399;
        margin-top: 10px;
        font-size: 14px;
    }

    @keyframes bounceIn {
        0% {
            transform: scale(0.9);
            opacity: 0;
        }

        50% {
            transform: scale(1.05);
        }

        100% {
            transform: scale(1);
            opacity: 1;
        }
    }

    @keyframes rotating {
        from {
            transform: rotate(0deg);
        }

        to {
            transform: rotate(360deg);
        }
    }
</style>