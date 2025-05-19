<template>
  <div class="app-container home">
    <div class="dashboard-container">
      <!-- 将图例说明移到日历上方 -->


      <div class="main-content">
        <div class="calendar-section">
          <el-calendar v-model="value">
            <template slot="dateCell" slot-scope="{date, data}">
              <div class="calendar-day-cell" @click="onDayClick(date)">
                <div class="day-number" :class="{ 
                  'is-today': isToday(date),
                  'has-meeting': hasMeeting(date)
                }">
                  {{ data.day.split('-')[2] }}
                </div>
                <div class="day-events">
                  <div v-for="(event, index) in getEventsForDate(date)" :key="index" class="event-label"
                    :class="event.type">
                    {{ event.name }}
                  </div>
                </div>
              </div>
            </template>
          </el-calendar>
        </div>
      </div>

      <!-- 日期详情弹窗 -->
      <el-dialog title="日期详情" :visible.sync="dialogVisible" width="30%" style="margin-top: 100px;"
        :before-close="handleClose">
        <div v-if="selectedDay">
          <h3>{{ formatDate(selectedDay.date) }}</h3>
          <div v-if="selectedDay.events.length > 0">
            <div v-for="(event, index) in selectedDay.events" :key="index" class="event-item">
              <span class="event-type" :class="event.type">{{ getEventTypeLabel(event.type) }}:</span>
              <span class="event-name">{{ event.name }}</span>
            </div>
          </div>
          <div v-else class="no-events">
            这一天没有特殊事件
          </div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
        </span>
      </el-dialog>
    </div>
  </div>
</template>

<script>
  import { ChineseHoliday, SolarTerm, MemorialDays } from '@/views/holidays'
  import { Solar, Lunar } from 'lunar-javascript'

  import { getUserFutureMeeting } from '@/api/meeting/meeting'

  export default {
    name: "Index",
    data() {
      return {
        value: new Date(),
        holidays: [],
        solarTerms: [],
        memorialDays: [],
        meetings: [], // 会议数据数组，初始为空
        dialogVisible: false,
        selectedDay: null
      }
    },
    mounted() {
      this.initCalendarData()
      this.fetchMeetingData()   // 修改为调用获取会议数据的方法
    },
    methods: {
      initCalendarData() {
        const currentYear = new Date().getFullYear()
        // 初始化25年的数据
        for (let year = currentYear; year < currentYear + 25; year++) {
          // 添加农历节日
          ChineseHoliday.forEach(holiday => {
            // 特殊处理除夕
            if (holiday.name === '除夕') {
              try {
                // 获取下一年正月初一
                const nextYearFirstDay = Lunar.fromYmd(year + 1, 1, 1)
                const solarDate = nextYearFirstDay.getSolar()

                // 创建正月初一的公历日期
                const firstDayDate = new Date(
                  solarDate.getYear(),
                  solarDate.getMonth() - 1,
                  solarDate.getDay()
                );

                // 计算前一天作为除夕
                const chuXiDate = new Date(firstDayDate);
                chuXiDate.setDate(firstDayDate.getDate() - 1);

                this.holidays.push({
                  date: chuXiDate,
                  name: '除夕'
                });
              } catch (e) {
                console.error(`除夕日期计算错误 (${year}年):`, e)
              }
            } else {
              // 处理其他农历节日
              try {
                const lunar = Lunar.fromYmd(year, holiday.month, holiday.day)
                const solar = lunar.getSolar()
                this.holidays.push({
                  date: new Date(solar.getYear(), solar.getMonth() - 1, solar.getDay()),
                  name: holiday.name
                })
              } catch (e) {
                console.error(`农历转换错误 (${year}年${holiday.month}月${holiday.day}日 - ${holiday.name}):`, e)
              }
            }
          })

          // 添加节气
          SolarTerm.forEach(term => {
            try {
              const solar = Solar.fromYmd(year, term.month, term.day)
              this.solarTerms.push({
                date: new Date(solar.getYear(), solar.getMonth() - 1, solar.getDay()),
                name: term.name
              })
            } catch (e) {
              console.error(`节气日期转换错误 (${year}年${term.month}月${term.day}日 - ${term.name}):`, e)
            }
          })

          // 添加纪念日
          MemorialDays.forEach(memorial => {
            try {
              this.memorialDays.push({
                date: new Date(year, memorial.month - 1, memorial.day),
                name: memorial.name
              })
            } catch (e) {
              console.error(`纪念日日期错误 (${year}年${memorial.month}月${memorial.day}日 - ${memorial.name}):`, e)
            }
          })
        }
      },
      async fetchMeetingData() {

        // 调用后端API获取会议数据
        await getUserFutureMeeting().then(response => {
          console.log("会议数据", response);

          if (response.code == 200) {
            // 处理返回的会议数据
            this.meetings = response.data.map(meeting => {
              return {
                ...meeting,
                date: new Date(meeting.startTime), // 使用startTime作为会议日期
                name: meeting.meetingName, // 用于显示的会议名称
                time: this.formatMeetingTime(meeting.startTime) // 格式化会议时间
              };
            });
          } else {
            this.$message.error('获取会议数据失败: ' + response.data.message);
          }
        })
          .catch(error => {
            console.error('获取会议数据出错:', error);
            this.$message.error('获取会议数据时发生错误，请稍后重试');
          })
          .finally(() => {
            // 关闭加载状态
            // this.$loading().close();
          });
      },
      formatMeetingTime(timeString) {
        if (!timeString) return '';
        const date = new Date(timeString);
        const hours = date.getHours().toString().padStart(2, '0');
        const minutes = date.getMinutes().toString().padStart(2, '0');
        return `${hours}:${minutes}`;
      },
      getEventsForDate(date) {
        const events = [];

        // 检查是否是今天
        if (this.isToday(date)) {
          events.push({
            type: 'today',
            name: '今天'
          });
        }

        // 检查是否有会议
        const meetingsForDate = this.getMeetingsForDate(date);
        meetingsForDate.forEach(meeting => {
          events.push({
            type: 'meeting',
            name: `${meeting.time} ${meeting.name}`
          });
        });

        // 检查节日
        this.holidays.forEach(holiday => {
          if (this.isSameDay(holiday.date, date)) {
            events.push({
              type: 'holiday',
              name: holiday.name
            });
          }
        });

        // 检查节气
        this.solarTerms.forEach(term => {
          if (this.isSameDay(term.date, date)) {
            events.push({
              type: 'solarTerm',
              name: term.name
            });
          }
        });

        // 检查纪念日
        this.memorialDays.forEach(memorial => {
          if (this.isSameDay(memorial.date, date)) {
            events.push({
              type: 'memorial',
              name: memorial.name
            });
          }
        });

        return events;
      },
      getMeetingsForDate(date) {
        // 获取当天的所有会议
        const meetingsOnDate = this.meetings.filter(meeting => this.isSameDay(meeting.date, date));

        // 如果没有会议，返回空数组
        if (meetingsOnDate.length === 0) {
          return [];
        }

        // 获取当前时间
        const now = new Date();

        // 对会议按开始时间排序
        meetingsOnDate.sort((a, b) => new Date(a.startTime) - new Date(b.startTime));

        // 找出未开始的会议中最近的一个
        const upcomingMeetings = meetingsOnDate.filter(meeting => new Date(meeting.startTime) > now);

        // 如果有未开始的会议，返回最近的一个；否则返回最后结束的一个
        return upcomingMeetings.length > 0 ? [upcomingMeetings[0]] : [meetingsOnDate[meetingsOnDate.length - 1]];
      },
      hasMeeting(date) {
        return this.getMeetingsForDate(date).length > 0;
      },
      isToday(date) {
        const today = new Date();
        return this.isSameDay(date, today);
      },
      onDayClick(date) {
        // 获取当天的所有事件
        const events = [];

        // 检查是否是今天
        if (this.isToday(date)) {
          events.push({
            type: 'today',
            name: '今天'
          });
        }

        // 检查是否有会议 - 这里显示所有会议，而不是只显示最近的一个
        const allMeetingsForDate = this.meetings.filter(meeting => this.isSameDay(meeting.date, date));
        allMeetingsForDate.forEach(meeting => {
          events.push({
            type: 'meeting',
            name: `${meeting.time} ${meeting.name}`
          });
        });

        // 检查节日
        this.holidays.forEach(holiday => {
          if (this.isSameDay(holiday.date, date)) {
            events.push({
              type: 'holiday',
              name: holiday.name
            });
          }
        });

        // 检查节气
        this.solarTerms.forEach(term => {
          if (this.isSameDay(term.date, date)) {
            events.push({
              type: 'solarTerm',
              name: term.name
            });
          }
        });

        // 检查纪念日
        this.memorialDays.forEach(memorial => {
          if (this.isSameDay(memorial.date, date)) {
            events.push({
              type: 'memorial',
              name: memorial.name
            });
          }
        });

        // 设置选中的日期和事件
        this.selectedDay = {
          date: date,
          events: events
        };

        // 显示弹窗
        this.dialogVisible = true;
      },
      isSameDay(date1, date2) {
        return date1.getDate() === date2.getDate() &&
          date1.getMonth() === date2.getMonth() &&
          date1.getFullYear() === date2.getFullYear();
      },
      formatDate(date) {
        const year = date.getFullYear();
        const month = date.getMonth() + 1;
        const day = date.getDate();
        return `${year}年${month}月${day}日`;
      },
      getEventTypeLabel(type) {
        switch (type) {
          case 'today':
            return '今天';
          case 'holiday':
            return '传统节日';
          case 'solarTerm':
            return '节气';
          case 'memorial':
            return '纪念日';
          case 'meeting':
            return '会议';
          default:
            return '';
        }
      },
      handleClose() {
        this.dialogVisible = false;
      }
    }
  }
</script>

<style scoped>
  .dashboard-container {
    height: 100vh;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  /* 修改图例容器样式，放在顶部 */
  .legend-container {
    display: flex;
    justify-content: center;
    padding: 10px;
    background: #fff;
    border-bottom: 1px solid #eee;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
    z-index: 1;
  }

  .main-content {
    flex: 1;
    display: flex;
    padding: 0 20px 20px;
    overflow: hidden;
  }

  .calendar-section {
    flex: 1;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    padding: 10px;
    overflow: hidden;
    display: flex;
    flex-direction: column;
  }

  /* 调整日历组件样式 */
  .calendar-section /deep/ .el-calendar {
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  .calendar-section /deep/ .el-calendar__header {
    padding: 10px 0;
  }

  .calendar-section /deep/ .el-calendar__body {
    flex: 1;
    overflow: auto;
    padding: 0;
  }

  .calendar-section /deep/ .el-calendar-table {
    height: 100%;
  }

  .calendar-section /deep/ .el-calendar-table .el-calendar-day {
    height: 100%;
  }

  .legend-item {
    display: flex;
    align-items: center;
    margin: 0 15px;
  }

  .dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    margin-right: 5px;
  }

  .highlight {
    width: 20px;
    height: 20px;
    border-radius: 4px;
    margin-right: 5px;
    /* 移除透明度，使用边框代替背景 */
    border: 2px solid;
    background: transparent;
  }


  .holiday {
    color: #FF4B4B;
    border-color: #FF4B4B;
  }

  .solar-term {
    color: #409EFF;
    border-color: #409EFF;
  }

  .memorial {
    color: #67C23A;
    border-color: #67C23A;
  }

  /* 自定义日期单元格样式 */
  .calendar-day-cell {
    min-height: 50px;
    height: 50px;
    display: flex;
    flex-direction: column;
    align-items: center;
    cursor: pointer;
    transition: all 0.2s;
    padding: 3px 0;
  }

  .calendar-day-cell:hover {
    background-color: rgba(0, 0, 0, 0.05);
    border-radius: 4px;
  }

  .day-number {
    font-size: 14px;
    margin-bottom: 3px;
  }

  .day-number.is-today {
    position: relative;
    font-weight: bold;
  }

  .day-number.is-today::after {
    content: '';
    position: absolute;
    width: 6px;
    height: 6px;
    background-color: #FF4B4B;
    border-radius: 50%;
    top: -3px;
    right: -8px;
  }

  .day-number.has-meeting {
    position: relative;
  }

  .day-number.has-meeting::before {
    content: '';
    position: absolute;
    width: 6px;
    height: 6px;
    background-color: #409EFF;
    border-radius: 50%;
    top: -3px;
    left: -3px;
  }

  /* 同时有今天和会议的情况 */
  .day-number.is-today.has-meeting::before {
    left: -8px;
  }

  .day-events {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 100%;
    overflow: hidden;
  }

  .event-label {
    font-size: 10px;
    line-height: 1.2;
    margin-top: 1px;
    padding: 1px 2px;
    border-radius: 2px;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .event-label.holiday {
    color: #FF4B4B;
    font-weight: bold;
  }

  .event-label.solarTerm {
    color: #409EFF;
    font-weight: bold;
  }

  .event-label.memorial {
    color: #67C23A;
    font-weight: bold;
  }

  .event-label.meeting {
    color: #409EFF;
    font-weight: bold;
  }

  .event-item {
    margin-bottom: 10px;
    padding: 5px 0;
    border-bottom: 1px dashed #eee;
  }

  .event-type {
    font-weight: bold;
    margin-right: 10px;
  }

  .event-type.holiday {
    color: #FF4B4B;
  }

  .event-type.solarTerm {
    color: #409EFF;
  }

  .event-type.memorial {
    color: #67C23A;
  }

  .event-type.meeting {
    color: #409EFF;
  }

  .event-type.today {
    color: #FF4B4B;
  }

  .event-name {
    font-size: 16px;
  }

  .no-events {
    color: #999;
    font-style: italic;
    text-align: center;
    padding: 20px 0;
  }

  @media (max-width: 992px) {
    .legend-container {
      flex-wrap: wrap;
      padding: 5px;
    }

    .legend-item {
      margin: 3px 8px;
    }

    .main-content {
      padding: 0 10px 10px;
    }

    .calendar-section {
      padding: 5px;
    }

    .calendar-day-cell {
      min-height: 50px;
      height: 50px;
      padding: 2px 0;
    }

    .day-number {
      font-size: 12px;
    }

    .event-label {
      font-size: 9px;
    }
  }
</style>