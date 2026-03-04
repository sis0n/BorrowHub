import { useState, useEffect } from "react";
import { Card } from "../components/ui/card";
import { Button } from "../components/ui/button";
import { Input } from "../components/ui/input";
import { Label } from "../components/ui/label";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from "../components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "../components/ui/select";
import { Plus, Search, Edit, Trash2, Package } from "lucide-react";
import { toast } from "sonner";

interface InventoryItem {
  id: string;
  name: string;
  type: string;
  status: "Available" | "Borrowed" | "Maintenance";
  quantity: number;
  available: number;
}

const inventoryData: InventoryItem[] = [
  {
    id: "1",
    name: "Projector - Epson EB-X41",
    type: "Equipment",
    status: "Available",
    quantity: 12,
    available: 9,
  },
  {
    id: "2",
    name: "Laptop - Dell XPS 15",
    type: "Laptop",
    status: "Borrowed",
    quantity: 20,
    available: 14,
  },
  {
    id: "3",
    name: "Camera - Canon EOS R6",
    type: "Equipment",
    status: "Available",
    quantity: 5,
    available: 3,
  },
  {
    id: "4",
    name: "Laptop - MacBook Pro 16",
    type: "Laptop",
    status: "Available",
    quantity: 10,
    available: 10,
  },
  {
    id: "5",
    name: "Conference Microphone",
    type: "Equipment",
    status: "Borrowed",
    quantity: 4,
    available: 0,
  },
  {
    id: "6",
    name: "Extension Cable 10m",
    type: "Equipment",
    status: "Available",
    quantity: 30,
    available: 28,
  },
  {
    id: "7",
    name: "Wireless Presenter",
    type: "Equipment",
    status: "Available",
    quantity: 8,
    available: 6,
  },
  {
    id: "8",
    name: "Portable Speaker",
    type: "Equipment",
    status: "Maintenance",
    quantity: 4,
    available: 2,
  },
  {
    id: "9",
    name: "Tripod Stand",
    type: "Equipment",
    status: "Available",
    quantity: 6,
    available: 5,
  },
  {
    id: "10",
    name: "HDMI Cable 5m",
    type: "Equipment",
    status: "Available",
    quantity: 25,
    available: 25,
  },
];

export function InventoryScreen() {
  const [currentUser, setCurrentUser] = useState<any>(null);
  const [inventory, setInventory] = useState<InventoryItem[]>([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [typeFilter, setTypeFilter] = useState("all");
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<string | null>(null);
  const [newItem, setNewItem] = useState({
    name: "",
    type: "",
    quantity: "",
    available: "",
  });

  useEffect(() => {
    const userStr = localStorage.getItem("borrowHubCurrentUser");
    if (userStr) {
      setCurrentUser(JSON.parse(userStr));
    }
    
    const inventoryStr = localStorage.getItem("borrowHubInventory");
    if (inventoryStr) {
      setInventory(JSON.parse(inventoryStr));
    } else {
      setInventory(inventoryData);
      localStorage.setItem("borrowHubInventory", JSON.stringify(inventoryData));
    }
  }, []);

  const isAdmin = currentUser?.role?.toLowerCase().includes("admin");

  const filteredData = inventory.filter((item) => {
    const matchesSearch = item.name.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesType = typeFilter === "all" || item.type === typeFilter;
    return matchesSearch && matchesType;
  });

  const getStatusColor = (status: string) => {
    switch (status) {
      case "Available":
        return "bg-green-50 text-green-700";
      case "Borrowed":
        return "bg-amber-50 text-amber-700";
      case "Maintenance":
        return "bg-red-50 text-red-700";
      default:
        return "bg-slate-50 text-slate-700";
    }
  };

  const handleEditClick = (item: InventoryItem) => {
    setEditingItem(item.id);
    setNewItem({
      name: item.name,
      type: item.type,
      quantity: item.quantity.toString(),
      available: item.available.toString(),
    });
    setIsDialogOpen(true);
  };

  const handleSaveItem = (e: React.FormEvent) => {
    e.preventDefault();
    
    let updatedInventory;
    
    if (editingItem) {
      updatedInventory = inventory.map((item) => 
        item.id === editingItem 
          ? {
              ...item,
              name: newItem.name,
              type: newItem.type,
              quantity: parseInt(newItem.quantity) || 0,
              available: parseInt(newItem.available) || 0,
            }
          : item
      );
      toast.success("Item Updated", {
        description: `${newItem.name} has been updated.`,
      });
    } else {
      const item: InventoryItem = {
        id: Date.now().toString(),
        name: newItem.name,
        type: newItem.type,
        status: "Available",
        quantity: parseInt(newItem.quantity) || 0,
        available: parseInt(newItem.available) || 0,
      };
      updatedInventory = [...inventory, item];
      toast.success("Item Added Successfully", {
        description: `${newItem.name} has been added to inventory.`,
      });
    }
    
    setInventory(updatedInventory);
    localStorage.setItem("borrowHubInventory", JSON.stringify(updatedInventory));
    
    setIsDialogOpen(false);
    setEditingItem(null);
    setNewItem({ name: "", type: "", quantity: "", available: "" });
  };

  const handleOpenAddDialog = () => {
    setEditingItem(null);
    setNewItem({ name: "", type: "", quantity: "", available: "" });
    setIsDialogOpen(true);
  };

  const handleDelete = (name: string) => {
    const updatedInventory = inventory.filter(item => item.name !== name);
    setInventory(updatedInventory);
    localStorage.setItem("borrowHubInventory", JSON.stringify(updatedInventory));
    
    toast.success("Item Deleted", {
      description: `${name} has been removed from inventory.`,
    });
  };

  return (
    <div className="max-w-6xl mx-auto space-y-6 animate-fade-in">
      {/* Premium Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 mb-2 tracking-tight">Inventory Management</h1>
          <p className="text-gray-500 text-base">Manage and track all resources</p>
        </div>
        {isAdmin && (
          <Button 
            className="bg-gradient-to-br from-black to-gray-800 hover:from-gray-800 hover:to-gray-900 sm:w-auto rounded-xl shadow-lg shadow-black/20 hover:shadow-xl hover:shadow-black/30 transition-all duration-300 h-11 px-5 font-medium" 
            onClick={handleOpenAddDialog}
          >
            <Plus className="w-4 h-4 mr-2" />
            Add Item
          </Button>
        )}
      </div>

      {/* Premium Filters Card */}
      <Card className="p-5 border-gray-200/60 shadow-sm hover:shadow-premium transition-all duration-300 rounded-2xl bg-white/80 backdrop-blur-sm">
        <div className="flex flex-col sm:flex-row gap-4">
          <div className="flex-1 relative">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <Input
              placeholder="Search by item name..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-11 h-11 border-gray-200 bg-gray-50 focus:bg-white rounded-xl transition-all"
            />
          </div>
          <Select value={typeFilter} onValueChange={setTypeFilter}>
            <SelectTrigger className="sm:w-52 h-11 border-gray-200 bg-gray-50 rounded-xl">
              <SelectValue placeholder="Filter by type" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Types</SelectItem>
              <SelectItem value="Equipment">Equipment</SelectItem>
              <SelectItem value="Laptop">Laptop</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </Card>

      {/* Premium Table Card */}
      <Card className="border-gray-200/60 overflow-hidden shadow-sm hover:shadow-premium transition-all duration-300 rounded-2xl bg-white/80 backdrop-blur-sm">
        {/* Desktop Table */}
        <div className="hidden md:block overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gradient-to-r from-gray-50 to-gray-100 border-b border-gray-200">
              <tr>
                <th className="text-left px-5 py-4 text-sm font-semibold text-gray-700">Item Name</th>
                <th className="text-left px-5 py-4 text-sm font-semibold text-gray-700">Type</th>
                <th className="text-left px-5 py-4 text-sm font-semibold text-gray-700">Status</th>
                <th className="text-right px-5 py-4 text-sm font-semibold text-gray-700">Available</th>
                <th className="text-right px-5 py-4 text-sm font-semibold text-gray-700">Total</th>
                {isAdmin && <th className="text-right px-5 py-4 text-sm font-semibold text-gray-700">Actions</th>}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {filteredData.map((item) => (
                <tr key={item.id} className="hover:bg-gray-50/80 transition-all duration-200 group">
                  <td className="px-5 py-4 text-sm font-medium text-gray-900">{item.name}</td>
                  <td className="px-5 py-4 text-sm text-gray-600">{item.type}</td>
                  <td className="px-5 py-4">
                    <span
                      className={`inline-flex items-center px-3 py-1.5 rounded-lg text-xs font-medium ${getStatusColor(
                        item.status
                      )}`}
                    >
                      {item.status}
                    </span>
                  </td>
                  <td className="px-5 py-4 text-sm font-semibold text-gray-900 text-right">{item.available}</td>
                  <td className="px-5 py-4 text-sm text-gray-600 text-right">{item.quantity}</td>
                  {isAdmin && (
                    <td className="px-5 py-4 text-right">
                      <div className="flex items-center justify-end gap-2">
                        <Button variant="ghost" size="sm" onClick={() => handleEditClick(item)} className="h-8 w-8 p-0 text-blue-600 hover:text-blue-700 hover:bg-blue-50">
                          <Edit className="w-4 h-4" />
                        </Button>
                        <Button variant="ghost" size="sm" onClick={() => handleDelete(item.name)} className="h-8 w-8 p-0 text-red-600 hover:text-red-700 hover:bg-red-50">
                          <Trash2 className="w-4 h-4" />
                        </Button>
                      </div>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Mobile Cards */}
        <div className="md:hidden divide-y divide-gray-100">
          {filteredData.map((item) => (
            <div key={item.id} className="p-5 hover:bg-gray-50/80 transition-all duration-200">
              <div className="flex items-start justify-between mb-3">
                <div className="text-sm font-semibold text-gray-900">{item.name}</div>
                <span
                  className={`inline-flex items-center px-3 py-1.5 rounded-lg text-xs font-medium ${getStatusColor(
                    item.status
                  )}`}
                >
                  {item.status}
                </span>
              </div>
              <div className="text-xs text-gray-500 mb-3 font-medium">{item.type}</div>
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-4 text-xs">
                  <span className="text-gray-600">
                    Available: <span className="text-gray-900 font-semibold">{item.available}</span>
                  </span>
                  <span className="text-gray-300">•</span>
                  <span className="text-gray-600">
                    Total: <span className="text-gray-900 font-semibold">{item.quantity}</span>
                  </span>
                </div>
                {isAdmin && (
                  <div className="flex items-center gap-1">
                    <Button variant="ghost" size="sm" onClick={() => handleEditClick(item)} className="h-8 w-8 p-0 text-blue-600">
                      <Edit className="w-4 h-4" />
                    </Button>
                    <Button variant="ghost" size="sm" onClick={() => handleDelete(item.name)} className="h-8 w-8 p-0 text-red-600">
                      <Trash2 className="w-4 h-4" />
                    </Button>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>

        {filteredData.length === 0 && (
          <div className="p-16 text-center">
            <div className="w-16 h-16 bg-gray-100 rounded-2xl flex items-center justify-center mx-auto mb-4">
              <Package className="w-8 h-8 text-gray-400" />
            </div>
            <p className="text-gray-500 font-medium">No items found matching your criteria.</p>
          </div>
        )}
      </Card>

      {/* Results Count */}
      <div className="flex items-center justify-between text-sm text-gray-500 px-1">
        <span className="font-medium">Showing {filteredData.length} of {inventory.length} items</span>
      </div>

      {/* Add/Edit Item Dialog - Premium Styling */}
      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent className="sm:max-w-[480px] rounded-2xl">
          <DialogHeader>
            <DialogTitle className="text-2xl font-bold">
              {editingItem ? "Edit Item" : "Add New Item"}
            </DialogTitle>
            <DialogDescription className="text-gray-500">
              {editingItem 
                ? "Update the details of the selected item."
                : "Enter the details of the new item to add it to the inventory."}
            </DialogDescription>
          </DialogHeader>
          <form onSubmit={handleSaveItem} className="space-y-5 mt-6">
            <div className="space-y-2">
              <Label htmlFor="name" className="text-gray-700 font-medium">Item Name</Label>
              <Input
                id="name"
                value={newItem.name}
                onChange={(e) => setNewItem({ ...newItem, name: e.target.value })}
                className="h-11 border-gray-200 bg-gray-50 focus:bg-white rounded-xl"
                placeholder="e.g., Laptop - Dell XPS 15"
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="type" className="text-gray-700 font-medium">Type</Label>
              <Select value={newItem.type} onValueChange={(val) => setNewItem({ ...newItem, type: val })} required>
                <SelectTrigger className="h-11 border-gray-200 bg-gray-50 rounded-xl">
                  <SelectValue placeholder="Select type" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="Equipment">Equipment</SelectItem>
                  <SelectItem value="Laptop">Laptop</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="quantity" className="text-gray-700 font-medium">Quantity</Label>
                <Input
                  id="quantity"
                  type="number"
                  value={newItem.quantity}
                  onChange={(e) => setNewItem({ ...newItem, quantity: e.target.value })}
                  className="h-11 border-gray-200 bg-gray-50 focus:bg-white rounded-xl"
                  placeholder="0"
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="available" className="text-gray-700 font-medium">Available</Label>
                <Input
                  id="available"
                  type="number"
                  value={newItem.available}
                  onChange={(e) => setNewItem({ ...newItem, available: e.target.value })}
                  className="h-11 border-gray-200 bg-gray-50 focus:bg-white rounded-xl"
                  placeholder="0"
                  required
                />
              </div>
            </div>
            <Button 
              type="submit" 
              className="w-full h-11 bg-gradient-to-br from-[#DC143C] to-[#B91130] hover:from-[#B91130] hover:to-[#9A0E28] rounded-xl shadow-lg shadow-[#DC143C]/25 hover:shadow-xl hover:shadow-[#DC143C]/30 transition-all duration-300 font-medium"
            >
              {editingItem ? "Save Changes" : "Add Item"}
            </Button>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}