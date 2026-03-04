import { useState, useEffect } from "react";
import { Card } from "../components/ui/card";
import { Input } from "../components/ui/input";
import { Button } from "../components/ui/button";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "../components/ui/tabs";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "../components/ui/select";
import { Search, Download, ArrowLeft, FileText, Activity } from "lucide-react";
import { useNavigate } from "react-router";

interface LogEntry {
  id: string;
  userId: string;
  userName: string;
  action: "Borrowed" | "Returned" | "Added" | "Updated" | "Deleted" | "Created User" | "Modified User";
  details: string;
  dateTime: string;
  performedBy: string;
  type: "Transaction" | "Activity";
}

const logsData: LogEntry[] = [
  {
    id: "1",
    userId: "EMP2045",
    userName: "Sarah Chen",
    action: "Borrowed",
    details: "Laptop - Dell XPS 15, Extension Cable 10m",
    dateTime: "Feb 18, 2026 09:15 AM",
    performedBy: "Staff (Maria Garcia)",
    type: "Transaction",
  },
  {
    id: "2",
    userId: "EMP1892",
    userName: "James Miller",
    action: "Returned",
    details: "Laptop - Dell XPS 15",
    dateTime: "Feb 18, 2026 08:45 AM",
    performedBy: "Staff (Maria Garcia)",
    type: "Transaction",
  },
  {
    id: "3",
    userId: "SYSTEM",
    userName: "Admin",
    action: "Added",
    details: "New Item: HDMI Cable 5m (Qty: 25)",
    dateTime: "Feb 17, 2026 04:30 PM",
    performedBy: "Admin (John Smith)",
    type: "Activity",
  },
  {
    id: "4",
    userId: "SYSTEM",
    userName: "Admin",
    action: "Updated",
    details: "Status change: Portable Speaker -> Maintenance",
    dateTime: "Feb 17, 2026 02:20 PM",
    performedBy: "Admin (John Smith)",
    type: "Activity",
  },
  {
    id: "5",
    userId: "STU3891",
    userName: "Lisa Thompson",
    action: "Borrowed",
    details: "Projector - Epson EB-X41",
    dateTime: "Feb 17, 2026 11:00 AM",
    performedBy: "Staff (Maria Garcia)",
    type: "Transaction",
  },
  {
    id: "6",
    userId: "SYSTEM",
    userName: "Admin",
    action: "Deleted",
    details: "Removed Item: Old VGA Cable",
    dateTime: "Feb 16, 2026 03:15 PM",
    performedBy: "Admin (John Smith)",
    type: "Activity",
  },
  {
    id: "7",
    userId: "EMP1523",
    userName: "Michael Brown",
    action: "Borrowed",
    details: "Wireless Presenter",
    dateTime: "Feb 15, 2026 10:30 AM",
    performedBy: "Staff (Maria Garcia)",
    type: "Transaction",
  },
  {
    id: "8",
    userId: "SYSTEM",
    userName: "Admin",
    action: "Modified User",
    details: "Reset password for Staff (Maria Garcia)",
    dateTime: "Feb 15, 2026 09:00 AM",
    performedBy: "Admin (John Smith)",
    type: "Activity",
  },
];

export function TransactionLogsScreen() {
  const navigate = useNavigate();
  const [logs, setLogs] = useState<LogEntry[]>([]);
  const [activeTab, setActiveTab] = useState("transaction");
  const [searchQuery, setSearchQuery] = useState("");
  const [actionFilter, setActionFilter] = useState("all");

  useEffect(() => {
    const logsStr = localStorage.getItem("borrowHubLogs");
    if (logsStr) {
      setLogs(JSON.parse(logsStr));
    } else {
      setLogs(logsData);
      localStorage.setItem("borrowHubLogs", JSON.stringify(logsData));
    }
  }, []);

  const filteredData = logs.filter((log) => {
    // Filter by Tab
    if (activeTab === "transaction" && log.type !== "Transaction") return false;
    if (activeTab === "activity" && log.type !== "Activity") return false;

    const matchesSearch =
      log.userName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      log.userId.toLowerCase().includes(searchQuery.toLowerCase()) ||
      log.details.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesAction = actionFilter === "all" || log.action === actionFilter;
    
    return matchesSearch && matchesAction;
  });

  const getActionColor = (action: string) => {
    switch (action) {
      case "Borrowed":
        return "bg-amber-50 text-amber-700";
      case "Returned":
        return "bg-green-50 text-green-700";
      case "Added":
      case "Created User":
        return "bg-blue-50 text-blue-700";
      case "Updated":
      case "Modified User":
        return "bg-purple-50 text-purple-700";
      case "Deleted":
        return "bg-red-50 text-red-700";
      default:
        return "bg-slate-50 text-slate-700";
    }
  };

  const getActionOptions = () => {
    if (activeTab === "transaction") {
      return (
        <>
          <SelectItem value="all">All Actions</SelectItem>
          <SelectItem value="Borrowed">Borrowed</SelectItem>
          <SelectItem value="Returned">Returned</SelectItem>
        </>
      );
    }
    return (
      <>
        <SelectItem value="all">All Actions</SelectItem>
        <SelectItem value="Added">Added</SelectItem>
        <SelectItem value="Updated">Updated</SelectItem>
        <SelectItem value="Deleted">Deleted</SelectItem>
        <SelectItem value="Created User">Created User</SelectItem>
        <SelectItem value="Modified User">Modified User</SelectItem>
      </>
    );
  };

  return (
    <div className="max-w-6xl mx-auto space-y-6 animate-fade-in">
      {/* Premium Header */}
      <div className="flex items-start justify-between gap-4 flex-wrap">
        <div className="flex items-center gap-4">
          <Button
            variant="ghost"
            size="sm"
            onClick={() => navigate("/app")}
            className="text-gray-600 hover:text-gray-900 hover:bg-gray-100 -ml-2 rounded-lg"
          >
            <ArrowLeft className="w-4 h-4" />
          </Button>
          <div>
            <h1 className="text-3xl font-bold text-gray-900 mb-2 tracking-tight">System Logs</h1>
            <p className="text-gray-500 text-base">Complete audit trail of all activities</p>
          </div>
        </div>
        <Button
          variant="outline"
          size="sm"
          className="border-gray-300 hover:bg-gray-100 rounded-lg h-10 px-4 font-medium"
        >
          <Download className="w-4 h-4 mr-2" />
          Export
        </Button>
      </div>

      <Tabs value={activeTab} onValueChange={(val) => { setActiveTab(val); setActionFilter("all"); }} className="w-full">
        <TabsList className="grid w-full max-w-md grid-cols-2 h-12 bg-gray-100 p-1 rounded-xl mb-6">
          <TabsTrigger value="transaction" className="rounded-lg font-medium data-[state=active]:bg-white data-[state=active]:shadow-sm">
            <FileText className="w-4 h-4 mr-2" />
            Transaction Logs
          </TabsTrigger>
          <TabsTrigger value="activity" className="rounded-lg font-medium data-[state=active]:bg-white data-[state=active]:shadow-sm">
            <Activity className="w-4 h-4 mr-2" />
            Activity Logs
          </TabsTrigger>
        </TabsList>

        {/* Premium Filters Card */}
        <Card className="p-5 border-gray-200/60 shadow-sm hover:shadow-premium transition-all duration-300 rounded-2xl bg-white/80 backdrop-blur-sm mb-6">
          <div className="flex flex-col sm:flex-row gap-4">
            <div className="flex-1 relative">
              <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
              <Input
                placeholder="Search logs..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-11 h-11 border-gray-200 bg-gray-50 focus:bg-white rounded-xl transition-all"
              />
            </div>
            <Select value={actionFilter} onValueChange={setActionFilter}>
              <SelectTrigger className="sm:w-52 h-11 border-gray-200 bg-gray-50 rounded-xl">
                <SelectValue placeholder="Filter by action" />
              </SelectTrigger>
              <SelectContent>
                {getActionOptions()}
              </SelectContent>
            </Select>
          </div>
        </Card>

        {/* Premium Table Card */}
        <Card className="border-gray-200/60 overflow-hidden shadow-sm hover:shadow-premium transition-all duration-300 rounded-2xl bg-white/80 backdrop-blur-sm">
          {/* Desktop Table */}
          <div className="hidden lg:block overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gradient-to-r from-gray-50 to-gray-100 border-b border-gray-200">
                <tr>
                  <th className="text-left px-5 py-4 text-sm font-semibold text-gray-700">
                    {activeTab === "transaction" ? "User/Student" : "System User"}
                  </th>
                  <th className="text-left px-5 py-4 text-sm font-semibold text-gray-700">Action</th>
                  <th className="text-left px-5 py-4 text-sm font-semibold text-gray-700">Details</th>
                  <th className="text-left px-5 py-4 text-sm font-semibold text-gray-700">Date & Time</th>
                  <th className="text-left px-5 py-4 text-sm font-semibold text-gray-700">Performed By</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {filteredData.map((log) => (
                  <tr key={log.id} className="hover:bg-gray-50/80 transition-all duration-200 group">
                    <td className="px-5 py-4">
                      <div className="text-sm font-medium text-gray-900">{log.userName}</div>
                      <div className="text-xs text-gray-500 font-medium">{log.userId}</div>
                    </td>
                    <td className="px-5 py-4">
                      <span
                        className={`inline-flex items-center px-3 py-1.5 rounded-lg text-xs font-medium ${getActionColor(
                          log.action
                        )}`}
                      >
                        {log.action}
                      </span>
                    </td>
                    <td className="px-5 py-4 text-sm font-medium text-gray-900">{log.details}</td>
                    <td className="px-5 py-4 text-sm text-gray-600 font-medium">{log.dateTime}</td>
                    <td className="px-5 py-4 text-sm text-gray-600 font-medium">{log.performedBy}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Mobile/Tablet Cards */}
          <div className="lg:hidden divide-y divide-gray-100">
            {filteredData.map((log) => (
              <div key={log.id} className="p-5 hover:bg-gray-50/80 transition-all duration-200">
                <div className="flex items-start justify-between gap-3 mb-3">
                  <div className="flex-1 min-w-0">
                    <div className="text-sm font-semibold text-gray-900 mb-1">{log.userName}</div>
                    <div className="text-xs text-gray-500 font-medium">{log.userId}</div>
                  </div>
                  <span
                    className={`inline-flex items-center px-3 py-1.5 rounded-lg text-xs font-medium ${getActionColor(
                      log.action
                    )}`}
                  >
                    {log.action}
                  </span>
                </div>
                <div className="text-sm font-medium text-gray-900 mb-3">{log.details}</div>
                <div className="flex items-center gap-2 text-xs text-gray-500 font-medium">
                  <span>{log.dateTime}</span>
                  <span>•</span>
                  <span>{log.performedBy}</span>
                </div>
              </div>
            ))}
          </div>

          {filteredData.length === 0 && (
            <div className="p-16 text-center">
              <div className="w-16 h-16 bg-gray-100 rounded-2xl flex items-center justify-center mx-auto mb-4">
                <FileText className="w-8 h-8 text-gray-400" />
              </div>
              <p className="text-gray-500 font-medium">No logs found matching your criteria.</p>
            </div>
          )}
        </Card>

        {/* Results Count */}
        <div className="flex items-center justify-between text-sm text-gray-500 px-1 mt-6">
          <span className="font-medium">Showing {filteredData.length} records</span>
        </div>
      </Tabs>
    </div>
  );
}