<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="82px">
      <el-form-item label="会议名称" prop="meetingName">
        <el-input v-model="queryParams.meetingName" placeholder="请输入会议名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="请假人" prop="userName">
        <el-input v-model="queryParams.userName" placeholder="请输入请假人" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="请假状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable>
          <el-option v-for="dict in opetionStatus " :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="审批时间" prop="updateTime">
        <el-date-picker v-model="dateRange" style="width: 240px" value-format="yyyy-MM-dd" type="daterange"
          range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期"></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" round @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" size="mini" round @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">

      <el-col :span="1.5">
        <el-button type="primary" round plain icon="el-icon-delete" size="mini" :disabled="multiple"
          @click="handleDelete" v-hasPermi="['meeting:leave:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" round plain icon="el-icon-download" size="mini" @click="handleExport"
          v-hasPermi="['meeting:leave:export']">导出</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="leaveList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" align="center" width="120">
        <template slot-scope="scope">
          {{ scope.$index + 1 + (queryParams.pageNum - 1) * queryParams.pageSize }}
        </template>
      </el-table-column>
      <el-table-column label="请假人" align="center" prop="userName" />
      <el-table-column label="会议名称" show-overflow-tooltip align="center" prop="meetingName" />
      <el-table-column label="请假理由" show-overflow-tooltip align="center" prop="reason" />
      <el-table-column label="请假状态" align="center" prop="status">
        <template slot-scope="scope">
          <!-- 根据状态值渲染不同标签 -->
          <el-tag :type="getStatusType(scope.row.status)" size="small">
            {{ getStatusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="请假时间" align="center" prop="createTime" width="150" />
      <el-table-column label="审批时间" align="center" prop="updateTime" width="150">
        <template slot-scope="scope">
          <span> {{scope.row.updateTime ? scope.row.updateTime :'暂无审批时间'}} </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" @click="handleUpdate(scope.row)"
            v-hasPermi="['meeting:leave:edit']">审批</el-button>
          <el-button size="mini" type="text" @click="handleDelete(scope.row)"
            v-hasPermi="['meeting:leave:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
      @pagination="getList" />

    <!-- 添加或修改请假对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="请假人" prop="userName">
          <el-input v-model="form.userName" placeholder="请输入请假人" :readonly="true" />
        </el-form-item>
        <el-form-item label="会议名称" prop="meetingName">
          <el-input v-model="form.meetingName" placeholder="请输入会议名称" :readonly="true" />
        </el-form-item>
        <el-form-item label="请假时间" prop="createTime">
          <el-input v-model="form.createTime" placeholder="请输入会议名称" :readonly="true" />
        </el-form-item>
        <el-form-item label="请假理由" prop="reason">
          <el-input v-model="form.reason" placeholder="请输入请假理由" :readonly="true" />
        </el-form-item>

      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleApproval(1)">批 准</el-button>
        <el-button @click="handleApproval(2)">拒 绝</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  import { listLeave, getLeave, delLeave, addLeave, updateLeave } from "@/api/meeting/leave";

  export default {
    name: "Index",
    data() {
      return {
        //日期范围查询
        dateRange: [],
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
        // 请假表格数据
        leaveList: [],
        // 弹出层标题
        title: "",
        // 是否显示弹出层
        open: false,
        // 查询参数
        queryParams: {
          pageNum: 1,
          pageSize: 10,
          userName: null,
          userId: null,
          meetingName: null,
          meetingId: null,
          reason: null,
          status: null,
          isDel: null,
        },
        // 表单参数
        form: {},
        // 表单校验
        rules: {
          userName: [
            { required: true, message: "请假人不能为空", trigger: "blur" }
          ],
          userId: [
            { required: true, message: "请假人编号不能为空", trigger: "blur" }
          ],
          meetingName: [
            { required: true, message: "会议名称不能为空", trigger: "blur" }
          ],
          meetingId: [
            { required: true, message: "会议编号不能为空", trigger: "blur" }
          ],
          reason: [
            { required: true, message: "请假理由不能为空", trigger: "blur" }
          ],
          status: [
            { required: true, message: "请假状态 0审批中 1通过 2拒绝不能为空", trigger: "change" }
          ],
          isDel: [
            { required: true, message: "逻辑删除不能为空", trigger: "blur" }
          ],
          createTime: [
            { required: true, message: "创建时间不能为空", trigger: "blur" }
          ],
          updateTime: [
            { required: true, message: "更新时间不能为空", trigger: "blur" }
          ]
        },
        opetionStatus: [
          {
            label: '审批中',
            value: '0'
          },
          {
            label: '批准',
            value: '1'
          },
          {
            label: '拒绝',
            value: '2'
          },
        ],
      };
    },
    created() {
      this.getList();
    },
    methods: {
      /** 查询请假列表 */
      getList() {
        this.loading = true;
        let params = this.addDateRange(this.queryParams, this.dateRange);
        listLeave(params).then(response => {
          this.leaveList = response.rows;
          this.total = response.total;
          this.loading = false;
        });
      },
      handleApproval(status) {
        this.form.status = status;
        this.submitForm();
      },
      // 取消按钮
      cancel() {
        this.open = false;
        this.reset();
      },
      // 表单重置
      reset() {
        this.form = {
          id: null,
          userName: null,
          userId: null,
          meetingName: null,
          meetingId: null,
          reason: null,
          status: null,
          isDel: null,
          createTime: null,
          updateTime: null
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
        this.dateRange = [];
        this.resetForm("queryForm");
        this.handleQuery();
      },
      // 多选框选中数据
      handleSelectionChange(selection) {
        this.ids = selection.map(item => item.id)
        this.single = selection.length !== 1
        this.multiple = !selection.length
      },
      /** 新增按钮操作 */
      handleAdd() {
        this.reset();
        this.open = true;
        this.title = "添加请假";
      },
      /** 修改按钮操作 */
      handleUpdate(row) {
        this.reset();
        const id = row.id || this.ids
        getLeave(id).then(response => {
          this.form = response.data;
          this.open = true;
          this.title = "审批请假";
        });
      },
      /** 提交按钮 */
      submitForm() {
        this.$refs["form"].validate(valid => {
          if (valid) {
            if (this.form.id != null) {
              updateLeave(this.form).then(response => {
                if (response.data.code === 200) {
                  this.$message.success(response.data.msg)
                }
                if (response.data.code === 500) {
                  this.$message.info(response.data.msg)
                }
                this.open = false;
                this.getList();
              });
            } else {
              addLeave(this.form).then(response => {
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
        const ids = row.id || this.ids;
        this.$modal.confirm('是否确认删除请假编号为"' + ids + '"的数据项？').then(function () {
          return delLeave(ids);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        }).catch(() => { });
      },
      /** 导出按钮操作 */
      handleExport() {
        this.download('/meeting/leave/export', {
          ...this.queryParams
        }, `leave_${new Date().getTime()}.xlsx`)
      },
      // 返回状态标签文本
      getStatusLabel(status) {
        switch (status) {
          case '0': return '审批中';
          case '1': return '批准';
          case '2': return '拒绝';
          case '3': return '已过期';

        }
      },
      // 返回对应的颜色类型
      getStatusType(status) {
        switch (status) {
          case '0': return 'warning';  // 黄色
          case '2': return 'danger';  // 绿色
          case '3': return 'info';  // 绿色

        }
      }
    }
  };
</script>