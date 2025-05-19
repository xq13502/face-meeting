<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="会议名称" prop="meetingName">
        <el-input v-model="queryParams.meetingName" placeholder="请输入会议名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="申请人" prop="userName">
        <el-input v-model="queryParams.userName" placeholder="请输入申请人" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="审批状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable>
          <el-option v-for="dict in opetionStatus " :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="审批时间" prop="approveTime">
        <el-date-picker v-model="dateRange" style="width: 240px" value-format="yyyy-MM-dd" type="daterange"
          range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期"></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" round size="mini" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" size="mini" round @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">

      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-delete" round size="mini" :disabled="multiple"
          @click="handleDelete" v-hasPermi="['meeting:approval:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-download" round size="mini" @click="handleExport"
          v-hasPermi="['meeting:approval:export']">导出</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="logList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <!-- <el-table-column label="审批编号" align="center" prop="id" /> -->
      <el-table-column label="序号" align="center" width="120">
        <template slot-scope="scope">
          {{ scope.$index + 1 + (queryParams.pageNum - 1) * queryParams.pageSize }}
        </template>
      </el-table-column>
      <el-table-column label="会议名称" show-overflow-tooltip align="center" prop="meetingName" />
      <el-table-column label="申请人" align="center" prop="userName" />
      <el-table-column label="开始时间" align="center" prop="cmsMeeting.startTime" width="150">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.cmsMeeting.startTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="结束时间" align="center" prop="cmsMeeting.endTime" width="150">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.cmsMeeting.endTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="审批状态" align="center" prop="status">
        <template slot-scope="scope">
          <!-- 根据状态值渲染不同标签 -->
          <el-tag :type="getStatusType(scope.row.status)" size="small">
            {{ getStatusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审批时间" align="center" prop="approveTime" width="150">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.approveTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="意见" show-overflow-tooltip align="center" prop="opinion" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" @click="handleUpdate(scope.row)"
            v-hasPermi="['meeting:approval:edit']">审批</el-button>
          <el-button size="mini" type="text" @click="handleDelete(scope.row)"
            v-hasPermi="['meeting:approval:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
      @pagination="getList" />

    <!-- 添加或修改审批记录对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="会议名称" prop="userName">
          <el-input v-model="form.meetingName" :readonly="true" />
        </el-form-item>
        <el-form-item label="申请人" prop="userName">
          <el-input v-model="form.userName" :readonly="true" />
        </el-form-item>
        <el-form-item label="开始时间" prop="cmsMeeting.startTime">
          <el-input v-model="form.cmsMeeting.startTime" :readonly="true" />
        </el-form-item>
        <el-form-item label="结束时间" prop="cmsMeeting.endTime">
          <el-input v-model="form.cmsMeeting.endTime" :readonly="true" />
        </el-form-item>
        <el-form-item label="意见" prop="opinion">
          <el-input v-model="form.opinion" type="textarea" placeholder="请输入内容" />
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
  import { listLog, getLog, delLog, addLog, updateLog } from "@/api/meeting/approval";

  export default {
    name: "Log",
    data() {
      return {
        //日期范围查询
        dateRange: [],
        // 遮罩层
        loading: true,
        // 选中数组
        ids: [],
        // 子表选中数据
        checkedMeetingReservation: [],
        // 非单个禁用
        single: true,
        // 非多个禁用
        multiple: true,
        // 显示查询条件
        showSearch: true,
        // 总条数
        total: 0,
        // 审批记录表格数据
        logList: [],
        // 会议预约表格数据
        meetingReservationList: [],
        // 弹出层标题
        title: "",
        // 是否显示弹出层
        open: false,
        // 查询参数
        queryParams: {
          pageNum: 1,
          pageSize: 10,
          meetingId: null,
          meetingName: null,
          userId: null,
          userName: null,
          approveTime: null,
          opinion: null
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
          {
            label: '已过期',
            value: '3'
          },
        ],
        // 表单参数
        form: {
          cmsMeeting: {
            startTime: '', // 确保 startTime 已初始化
            endTime: ''
          },
        },
        // 表单校验
        rules: {
          meetingId: [
            { required: true, message: "预约编号不能为空", trigger: "blur" }
          ],
          userName: [
            { required: true, message: "申请人不能为空", trigger: "blur" }
          ],
        },
        pickerOptions: {
          shortcuts: [
            {
              text: '半小时后',
              onClick(picker) {
                const date = new Date();
                date.setTime(date.getTime() + 30 * 60 * 1000); // 半小时
                picker.$emit('pick', date);
              }
            },
            {
              text: '一小时后',
              onClick(picker) {
                const date = new Date();
                date.setTime(date.getTime() + 60 * 60 * 1000); // 一小时
                picker.$emit('pick', date);
              }
            },
            {
              text: '明天此刻',
              onClick(picker) {
                const date = new Date();
                date.setTime(date.getTime() + 24 * 60 * 60 * 1000); // 明天
                picker.$emit('pick', date);
              }
            }
          ],
          disabledDate(time) {
            // 限制只能选择今天之后的日期
            return time.getTime() < Date.now() - 8.64e7; // 8.64e7 是一天的毫秒数
          }
        },
      };
    },
    created() {
      this.getList();
    },
    methods: {
      /** 查询审批记录列表 */
      getList() {
        this.loading = true;
        // 添加日志输出来检查日期范围
        let params = this.addDateRange(this.queryParams, this.dateRange);

        // 处理日期范围
        if (this.dateRange && this.dateRange.length === 2) {
          params.params.beginTime = this.dateRange[0] + ' 00:00:00';
          params.params.endTime = this.dateRange[1] + ' 00:00:00';
        }
        listLog(params).then(response => {
          console.log("审批记录列表", response);

          this.logList = response.rows;
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
          meetingId: null,
          userName: null,
          approveTime: null,
          opinion: null,
          cmsMeeting: {
            startTime: '',
            endTime: ''
          }
        };
        this.meetingReservationList = [];
        this.resetForm("form");
      },
      /** 查询按钮操作 */
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
        this.title = "添加审批记录";
      },
      /** 修改按钮操作 */
      handleUpdate(row) {
        this.reset();
        const id = row.id || this.ids
        getLog(id).then(response => {
          console.log("当前审批会议的数据", response.data);

          this.form = response.data;
          this.meetingReservationList = response.data.meetingReservationList;
          this.open = true;
          this.title = "审批会议";
        });
      },
      /** 提交按钮 */
      submitForm() {
        this.$refs["form"].validate(valid => {
          if (valid) {
            this.form.meetingReservationList = this.meetingReservationList;
            if (this.form.id != null) {
              updateLog(this.form).then(response => {
                if (response.data.code === 200) {
                  this.$message.success(response.data.msg)
                }
                else if (response.data.code === 500) {
                  this.$message.info(response.data.msg)
                }
                this.open = false;
                this.getList();
              });
            } else {
              addLog(this.form).then(response => {
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
        this.$modal.confirm('是否确认删除审批记录编号为"' + ids + '"的数据项？').then(function () {
          return delLog(ids);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        }).catch(() => { });
      },
      /** 会议预约序号 */
      rowMeetingReservationIndex({ row, rowIndex }) {
        row.index = rowIndex + 1;
      },
      /** 会议预约添加按钮操作 */
      handleAddMeetingReservation() {
        let obj = {};
        obj.title = "";
        obj.roomId = "";
        obj.startTime = "";
        obj.endTime = "";
        obj.applicantId = "";
        obj.participants = "";
        obj.approvalStatus = "";
        obj.approvalBy = "";
        obj.approvalTime = "";
        this.meetingReservationList.push(obj);
      },
      /** 会议预约删除按钮操作 */
      handleDeleteMeetingReservation() {
        if (this.checkedMeetingReservation.length == 0) {
          this.$modal.msgError("请先选择要删除的会议预约数据");
        } else {
          const meetingReservationList = this.meetingReservationList;
          const checkedMeetingReservation = this.checkedMeetingReservation;
          this.meetingReservationList = meetingReservationList.filter(function (item) {
            return checkedMeetingReservation.indexOf(item.index) == -1
          });
        }
      },
      /** 复选框选中数据 */
      handleMeetingReservationSelectionChange(selection) {
        this.checkedMeetingReservation = selection.map(item => item.index)
      },
      /** 导出按钮操作 */
      handleExport() {
        this.download('meeting/approval/export', {
          ...this.queryParams
        }, `log_${new Date().getTime()}.xlsx`)
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
          case '0': return 'warning';  // 绿色
          case '2': return 'danger';  // 绿色
          case '3': return 'info';  // 绿色

        }
      }
    }
  };
</script>