import { useState, useEffect } from "react";
import { Card } from "../components/ui/card";
import { Button } from "../components/ui/button";
import { Input } from "../components/ui/input";
import { Label } from "../components/ui/label";
import { toast } from "sonner";
import { User, Lock, Shield } from "lucide-react";

export function AccountSettingsScreen() {
  const [currentUser, setCurrentUser] = useState<any>(null);
  const [name, setName] = useState("");
  const [username, setUsername] = useState("");
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  useEffect(() => {
    const userStr = localStorage.getItem("borrowHubCurrentUser");
    if (userStr) {
      const user = JSON.parse(userStr);
      setCurrentUser(user);
      setName(user.name || "");
      setUsername(user.username || "");
    }
  }, []);

  const handleUpdateProfile = (e: React.FormEvent) => {
    e.preventDefault();
    if (!currentUser) return;

    let users: any[] = [];
    const usersStr = localStorage.getItem("borrowHubUsers");
    if (usersStr) {
      users = JSON.parse(usersStr);
      if (username !== currentUser.username) {
        const usernameExists = users.some((u: any) => u.username === username);
        if (usernameExists) {
          toast.error("Error", { description: "Username already exists." });
          return;
        }
      }
    }

    const updatedUser = { ...currentUser, name, username };

    if (usersStr) {
      const userIndex = users.findIndex((u: any) => u.username === currentUser.username);
      if (userIndex !== -1) {
        users[userIndex].name = name;
        users[userIndex].username = username;
        localStorage.setItem("borrowHubUsers", JSON.stringify(users));
      }
    }

    setCurrentUser(updatedUser);
    localStorage.setItem("borrowHubCurrentUser", JSON.stringify(updatedUser));

    toast.success("Profile Updated", {
      description: "Your profile information has been saved.",
    });
  };

  const handleChangePassword = (e: React.FormEvent) => {
    e.preventDefault();
    if (!currentUser) return;

    if (currentPassword !== currentUser.password) {
      toast.error("Error", { description: "Current password is incorrect." });
      return;
    }
    if (newPassword !== confirmPassword) {
      toast.error("Error", { description: "New passwords do not match." });
      return;
    }

    const updatedUser = { ...currentUser, password: newPassword };
    setCurrentUser(updatedUser);
    localStorage.setItem("borrowHubCurrentUser", JSON.stringify(updatedUser));

    const usersStr = localStorage.getItem("borrowHubUsers");
    if (usersStr) {
      const users = JSON.parse(usersStr);
      const userIndex = users.findIndex((u: any) => u.username === currentUser.username);
      if (userIndex !== -1) {
        users[userIndex].password = newPassword;
        localStorage.setItem("borrowHubUsers", JSON.stringify(users));
      }
    }

    toast.success("Password Changed", {
      description: "Your password has been successfully updated.",
    });

    setCurrentPassword("");
    setNewPassword("");
    setConfirmPassword("");
  };

  if (!currentUser) return null;

  return (
    <div className="max-w-3xl mx-auto space-y-6 animate-fade-in">
      <div>
        <h1 className="text-3xl font-bold text-gray-900 mb-2 tracking-tight">Account Settings</h1>
        <p className="text-gray-500 text-base">Manage your profile and security preferences</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Profile Card */}
        <Card className="p-6 md:col-span-1 border-gray-200/60 shadow-sm rounded-2xl bg-white/80 backdrop-blur-sm h-fit">
          <div className="flex flex-col items-center text-center space-y-4">
            <div className="w-24 h-24 bg-gradient-to-br from-gray-100 to-gray-200 rounded-full flex items-center justify-center border-4 border-white shadow-sm">
              <User className="w-10 h-10 text-gray-400" />
            </div>
            <div>
              <h2 className="text-xl font-semibold text-gray-900">{currentUser.name}</h2>
              <div className="inline-flex items-center justify-center px-3 py-1 mt-2 bg-blue-50 text-blue-700 rounded-full text-xs font-medium">
                <Shield className="w-3 h-3 mr-1.5" />
                {currentUser.role}
              </div>
            </div>
          </div>
        </Card>

        {/* Forms Container */}
        <div className="md:col-span-2 space-y-6">
          {/* General Information Form */}
          <Card className="p-6 border-gray-200/60 shadow-sm rounded-2xl bg-white/80 backdrop-blur-sm">
            <h3 className="text-lg font-semibold text-gray-900 mb-5 flex items-center">
              <User className="w-5 h-5 mr-2 text-gray-500" />
              General Information
            </h3>
            <form onSubmit={handleUpdateProfile} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="username" className="text-gray-700">Username</Label>
                <Input
                  id="username"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  className="h-11 rounded-xl focus:border-[#DC143C] focus:ring-[#DC143C]/20"
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="role" className="text-gray-700">Role (Uneditable)</Label>
                <Input
                  id="role"
                  value={currentUser.role}
                  disabled
                  className="bg-gray-100/50 text-gray-500 h-11 rounded-xl"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="name" className="text-gray-700">Full Name</Label>
                <Input
                  id="name"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  className="h-11 rounded-xl focus:border-[#DC143C] focus:ring-[#DC143C]/20"
                  required
                />
              </div>
              <Button type="submit" className="bg-gray-900 hover:bg-gray-800 text-white rounded-xl h-11 px-6">
                Save Changes
              </Button>
            </form>
          </Card>

          {/* Change Password Form */}
          <Card className="p-6 border-gray-200/60 shadow-sm rounded-2xl bg-white/80 backdrop-blur-sm">
            <h3 className="text-lg font-semibold text-gray-900 mb-5 flex items-center">
              <Lock className="w-5 h-5 mr-2 text-gray-500" />
              Change Password
            </h3>
            <form onSubmit={handleChangePassword} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="currentPassword" className="text-gray-700">Current Password</Label>
                <Input
                  id="currentPassword"
                  type="password"
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  className="h-11 rounded-xl focus:border-[#DC143C] focus:ring-[#DC143C]/20"
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="newPassword" className="text-gray-700">New Password</Label>
                <Input
                  id="newPassword"
                  type="password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  className="h-11 rounded-xl focus:border-[#DC143C] focus:ring-[#DC143C]/20"
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="confirmPassword" className="text-gray-700">Confirm New Password</Label>
                <Input
                  id="confirmPassword"
                  type="password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  className="h-11 rounded-xl focus:border-[#DC143C] focus:ring-[#DC143C]/20"
                  required
                />
              </div>
              <Button type="submit" variant="outline" className="rounded-xl h-11 px-6 border-gray-300">
                Update Password
              </Button>
            </form>
          </Card>
        </div>
      </div>
    </div>
  );
}