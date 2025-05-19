<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="82px">
      <el-form-item label="消息发送者" prop="sendName">
        <el-input v-model="queryParams.sendName" placeholder="请输入发送者" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="消息接收者" prop="acceptName">
        <el-input v-model="queryParams.acceptName" placeholder="请输入接收者" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>

      <el-form-item label="消息类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择消息类型" clearable>
          <el-option v-for="dict in opetionType " :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>

      <el-form-item label="是否已读" prop="isRead">
        <el-select v-model="queryParams.isRead" placeholder="请选择是否已读" clearable>
          <el-option v-for="dict in opetionStatus " :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" icon="el-icon-search" round size="mini" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" size="mini" round @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" round plain icon="el-icon-download" size="mini" @click="handleExport"
          v-hasPermi="['meeting:message:export']">导出</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="messageList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" align="center" width="120">
        <template slot-scope="scope">
          {{ scope.$index + 1 + (queryParams.pageNum - 1) * queryParams.pageSize }}
        </template>
      </el-table-column>
      <el-table-column label="发送者" align="center" prop="sendName" />
      <el-table-column label="接收者" align="center" prop="acceptName" />
      <el-table-column label="消息类型" align="center" prop="type">
        <template slot-scope="scope">
          <!-- 根据状态值渲染不同标签 -->
          <el-tag :type="getType(scope.row.type)" size="small">
            {{ getLabel(scope.row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发送内容" show-overflow-tooltip align="center" prop="content" />
      <el-table-column label="状态" align="center" prop="isRead">
        <template slot-scope="scope">
          <!-- 根据状态值渲染不同标签 -->
          <el-tag :type="getStatusType(scope.row.isRead)" size="small">
            {{ getStatusLabel(scope.row.isRead) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发送时间" align="center" prop="createTime" width="150" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" @click="handleUpdate(scope.row)"
            v-hasPermi="['meeting:message:edit']">修改</el-button>
          <el-button size="mini" type="text" @click="handleDelete(scope.row)"
            v-hasPermi="['meeting:message:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
      @pagination="getList" />

    <!-- 添加或修改消息对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">

        <el-form-item label="发送者" prop="sendName">
          <el-input v-model="form.sendName" placeholder="请输入发送者" :readonly="true" />
        </el-form-item>
        <el-form-item label="接收者" prop="acceptName">
          <el-input v-model="form.acceptName" placeholder="请输入接收者" :readonly="true" />
        </el-form-item>
        <el-form-item label="发送内容">
          <editor v-model="form.content" :min-height="192" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  import { listMessage, getMessage, delMessage, addMessage, updateMessage } from "@/api/meeting/message";

  export default {
    name: "Message",
    data() {
      return {
        // 遮罩层
        loading: true,
        // 选中数组
        ids: [],
        // 非单个禁用
        single: true,
        // 非多个禁用
        multiple: true,
        // 显示搜索条件
        showSearch: true,
        // 总条数
        total: 0,
        // 消息表格数据
        messageList: [],
        // 弹出层标题
        title: "",
        // 是否显示弹出层
        open: false,
        // 查询参数
        queryParams: {
          pageNum: 1,
          pageSize: 10,
          sendId: null,
          sendName: null,
          acceptId: null,
          accpetName: null,
          content: null,
          type: null,
          isRead: null
        },
        opetionStatus: [
          {
            label: "未读",
            value: "0"
          },
          {
            label: "已读",
            value: '1'
          }
        ],
        opetionType: [
          {
            label: "系统通知",
            value: "0"
          },
          {
            label: "会议通知",
            value: '1'
          }
        ],

        // 表单参数
        form: {},
        // 表单校验
        rules: {
          sendId: [
            { required: true, message: "发送者编号不能为空", trigger: "blur" }
          ],
          sendName: [
            { required: true, message: "发送者姓名不能为空", trigger: "blur" }
          ],
          acceptId: [
            { required: true, message: "接收者编号不能为空", trigger: "blur" }
          ],
          type: [
            { required: true, message: "消息类型不能为空", trigger: "change" }
          ],
          createTime: [
            { required: true, message: "创建时间不能为空", trigger: "blur" }
          ],
          updateTime: [
            { required: true, message: "更新时间不能为空", trigger: "blur" }
          ],
          isDel: [
            { required: true, message: "逻辑删除不能为空", trigger: "blur" }
          ],
          isRead: [
            { required: true, message: "状态不能为空", trigger: "blur" }
          ]
        }
      };
    },
    created() {
      this.getList();
    },
    methods: {
      /** 查询消息列表 */
      getList() {
        this.loading = true;
        listMessage(this.queryParams).then(response => {
          this.messageList = response.rows;
          this.total = response.total;
          this.loading = false;
        });
      },
      // 取消按钮
      cancel() {
        this.open = false;
        this.reset();
      },
      // 表单重置
      reset() {
        this.form = {
          messageId: null,
          sendId: null,
          sendName: null,
          acceptId: null,
          acceptName: null,
          content: null,
          type: null,
          createTime: null,
          updateTime: null,
          isDel: null,
          isRead: null
        };
        this.resetForm("form");
      },
      /** 搜索按钮操作 */
      handleQuery() {
        this.queryParams.pageNum = 1;
        this.getList();
      },
      /** 重置按钮操作 */
      resetQuery() {
        this.resetForm("queryForm");
        this.handleQuery();
      },
      // 多选框选中数据
      handleSelectionChange(selection) {
        this.ids = selection.map(item => item.messageId)
        this.single = selection.length !== 1
        this.multiple = !selection.length
      },
      /** 新增按钮操作 */
      handleAdd() {
        this.reset();
        this.open = true;
        this.title = "添加消息";
      },
      /** 修改按钮操作 */
      handleUpdate(row) {
        this.reset();
        const messageId = row.messageId || this.ids
        getMessage(messageId).then(response => {
          this.form = response.data;
          this.open = true;
          this.title = "修改消息";
        });
      },
      /** 提交按钮 */
      submitForm() {
        this.$refs["form"].validate(valid => {
          if (valid) {
            if (this.form.messageId != null) {
              updateMessage(this.form).then(response => {
                this.$modal.msgSuccess("修改成功");
                this.open = false;
                this.getList();
              });
            } else {
              addMessage(this.form).then(response => {
                this.$modal.msgSuccess("新增成功");
                this.open = false;
                this.getList();
              });
            }
          }
        });
      },
      /** 删除按钮操作 */
      handleDelete(row) {
        const messageIds = row.messageId || this.ids;
        this.$modal.confirm('是否确认删除消息编号为"' + messageIds + '"的数据项？').then(function () {
          return delMessage(messageIds);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        }).catch(() => { });
      },
      /** 导出按钮操作 */
      handleExport() {
        this.download('/meeting/message/export', {
          ...this.queryParams
        }, `message_${new Date().getTime()}.xlsx`)
      },
      // 返回状态标签文本
      getStatusLabel(status) {
        switch (status) {
          case 0: return '未读';
          case 1: return '已读';

        }
      },
      // 返回对应的颜色类型
      getStatusType(status) {
        switch (status) {
          case 0: return 'danger';  // 绿色

        }
      },
      getLabel(type) {
        switch (type) {
          case '0': return '系统通知';
          case '1': return '会议通知';

        }
      },
      // 返回对应的颜色类型
      getType(type) {
        switch (type) {
          case '0': return 'info';  // 绿色

        }
      }
    }
  };
</script>