import React, { useState, useEffect } from 'react';
import { 
  PieChart, LayoutDashboard, Users, Plus, LogOut, 
  Search, Receipt, IndianRupee, CheckCircle, ArrowRight, History, 
  X, AlertCircle, Trash2, Download, UserMinus, LogOut as LeaveIcon
} from 'lucide-react';
import api from './api';
import './App.css';

// --- MAIN APP COMPONENT ---
function App() {
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [user, setUser] = useState(JSON.parse(localStorage.getItem('user')));
  const [activeGroupId, setActiveGroupId] = useState(null);
  const [refreshTrigger, setRefreshTrigger] = useState(false);

  // Global Alert State
  const [alertInfo, setAlertInfo] = useState(null); 

  const showAlert = (title, msg, type='error') => setAlertInfo({title, msg, type});
  const closeAlert = () => setAlertInfo(null);

  const handleLogin = (token, userData) => {
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(userData));
    setToken(token);
    setUser(userData);
  };

  const logout = () => {
    localStorage.clear();
    setToken(null);
    setUser(null);
    setActiveGroupId(null);
  };

  const triggerRefresh = () => setRefreshTrigger(!refreshTrigger);

  if (!token) return (
    <>
      <AuthPage onLogin={handleLogin} showAlert={showAlert} />
      {alertInfo && <AlertModal info={alertInfo} onClose={closeAlert} />}
    </>
  );

  return (
    <div className="app-container">
      <Sidebar 
        user={user} 
        activeGroupId={activeGroupId} 
        setActiveGroupId={setActiveGroupId} 
        onLogout={logout} 
        refreshTrigger={refreshTrigger}
        onGroupCreate={triggerRefresh}
        showAlert={showAlert}
      />
      <main className="main-content">
        {activeGroupId ? (
          <GroupView 
            groupId={activeGroupId} 
            currentUser={user} 
            showAlert={showAlert}
            // When leaving/deleting, go back to dashboard & refresh list
            onExitGroup={() => { setActiveGroupId(null); triggerRefresh(); }}
          />
        ) : (
          <Dashboard 
            currentUser={user} 
            setActiveGroupId={setActiveGroupId} 
            refreshTrigger={refreshTrigger}
          />
        )}
      </main>

      {alertInfo && <AlertModal info={alertInfo} onClose={closeAlert} />}
    </div>
  );
}

// --- SIDEBAR ---
function Sidebar({ user, activeGroupId, setActiveGroupId, onLogout, refreshTrigger, onGroupCreate, showAlert }) {
  const [myGroups, setMyGroups] = useState([]);
  const [searchResults, setSearchResults] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [showCreateGroup, setShowCreateGroup] = useState(false);

  useEffect(() => {
    api.get('/groups/my-groups').then(res => setMyGroups(res.data)).catch(console.error);
  }, [refreshTrigger]);

  const handleSearch = async (e) => {
    const term = e.target.value;
    setSearchTerm(term);
    if(term.length > 2) {
        try { const res = await api.get(`/groups/search?name=${term}`); setSearchResults(res.data); } 
        catch(err) { console.error(err); }
    } else { setSearchResults([]); }
  };

  const createGroup = async (name) => {
    if(!name) return;
    try { 
        await api.post('/groups', {name}); 
        onGroupCreate(); 
        setShowCreateGroup(false);
        showAlert("Success", `Group "${name}" created!`, "success");
    } catch(e) { showAlert("Error", "Failed to create group."); }
  };

  return (
    <aside className="sidebar">
      <div className="brand"><PieChart size={24} /> FairShare</div>
      
      <div className="search-container">
         <input className="search-input" placeholder="Search groups..." value={searchTerm} onChange={handleSearch}/>
      </div>

      <div className={`nav-item ${!activeGroupId ? 'active' : ''}`} onClick={() => setActiveGroupId(null)}>
        <LayoutDashboard size={18} /> Dashboard
      </div>

      {searchResults.length > 0 && <div className="section-label">Search Results</div>}
      {searchResults.map(g => (
        <div key={g.id} className="nav-item" onClick={() => {setActiveGroupId(g.id); setSearchTerm(''); setSearchResults([])}}>
           <Search size={16} color="#1cc29f" /> {g.name}
        </div>
      ))}

      <div className="section-label">My Groups</div>
      {myGroups.map(g => (
        <div key={g.id} className={`nav-item ${activeGroupId === g.id ? 'active' : ''}`} onClick={() => setActiveGroupId(g.id)}>
           <Users size={16} /> {g.name}
        </div>
      ))}

      <div className="nav-item" onClick={() => setShowCreateGroup(true)} style={{color: '#1cc29f', marginTop: 10}}>
        <Plus size={18} /> Add Group
      </div>

      <div style={{marginTop: 'auto', padding: '20px'}}>
          <div style={{color: '#fff', fontWeight: 'bold', marginBottom: 5}}>{user.name}</div>
          <div onClick={onLogout} style={{color: '#ff652f', cursor: 'pointer', display:'flex', gap: 5, fontSize: '0.85rem'}}>
            <LogOut size={14} /> Log out
          </div>
      </div>

      {showCreateGroup && <InputModal title="Create New Group" placeholder="Enter group name" onClose={() => setShowCreateGroup(false)} onSubmit={createGroup} />}
    </aside>
  );
}

// --- DASHBOARD ---
function Dashboard({ currentUser, setActiveGroupId, refreshTrigger }) {
    const [groups, setGroups] = useState([]);

    useEffect(() => {
        api.get('/groups/my-groups').then(res => setGroups(res.data));
    }, [refreshTrigger]);

    return (
        <div className="center-panel">
            <div className="header">
                <h2>Dashboard</h2>
                <div style={{color: '#888'}}>Welcome, {currentUser.name}</div>
            </div>
            <div style={{padding: '20px'}}>
                <div className="section-label" style={{marginBottom: 10}}>Your Groups</div>
                {groups.length === 0 ? <p style={{padding: 20, color: '#666'}}>No groups yet.</p> :
                    groups.map(g => (
                        <div key={g.id} className="group-card" onClick={() => setActiveGroupId(g.id)}>
                            <div className="group-icon"><Users size={20} /></div>
                            <div style={{flex: 1, fontWeight: 'bold'}}>{g.name}</div>
                            <ArrowRight size={16} color="#666" />
                        </div>
                    ))
                }
            </div>
        </div>
    );
}

// --- GROUP VIEW (Updated with Leave Group) ---
function GroupView({ groupId, currentUser, showAlert, onExitGroup }) {
  const [group, setGroup] = useState(null);
  const [balances, setBalances] = useState(null);
  const [history, setHistory] = useState([]);
  const [refresh, setRefresh] = useState(false);
  
  // Modals
  const [showAddExpense, setShowAddExpense] = useState(false);
  const [showAddMember, setShowAddMember] = useState(false);
  const [settleData, setSettleData] = useState(null);
  
  const [expandedId, setExpandedId] = useState(null);
  const [expandedData, setExpandedData] = useState(null);

  useEffect(() => {
    Promise.all([
        api.get(`/groups/${groupId}`),
        api.get(`/groups/${groupId}/balances`),
        api.get(`/audit/group/${groupId}`)
    ]).then(([g, b, h]) => {
        setGroup(g.data); setBalances(b.data); setHistory(h.data);
    }).catch(console.error);
  }, [groupId, refresh]);

  const getUserName = (id) => {
      if (!balances || !balances.balances) return `User ${id}`;
      const found = balances.balances.find(b => b.userId === id);
      return found ? found.name : `User ${id}`;
  };

  const toggleExpand = async (logId, expenseId) => {
      if (expandedId === logId) {
          setExpandedId(null); 
      } else {
          setExpandedId(logId);
          setExpandedData(null);
          try {
              const res = await api.get(`/expenses/${expenseId}`);
              setExpandedData(res.data);
          } catch(e) { console.error("Failed details"); }
      }
  };

  const openSettleModal = (toUser, amount) => {
      setSettleData({ groupId, toUser, toName: getUserName(toUser), amount });
  };

  const addMember = async (email) => {
    try { 
        await api.post(`/groups/${groupId}/add-member`, {email}); 
        setRefresh(!refresh);
        setShowAddMember(false);
        showAlert("Success", "Member added!", "success");
    }
    catch(e) { showAlert("Error", "Could not add member."); }
  };

  const handleExport = async () => {
      try {
          const response = await api.get(`/groups/${groupId}/export`, { responseType: 'blob' });
          const url = window.URL.createObjectURL(new Blob([response.data]));
          const link = document.createElement('a');
          link.href = url;
          link.setAttribute('download', `${group.name}_Export.csv`);
          document.body.appendChild(link);
          link.click();
      } catch (e) { showAlert("Error", "Export failed"); }
  };

  // --- NEW: DELETE GROUP ---
  const handleDeleteGroup = async () => {
      if(!window.confirm("ARE YOU SURE? This deletes everything.")) return;
      try {
          await api.delete(`/groups/${groupId}`);
          onExitGroup();
      } catch(e) { showAlert("Error", "Could not delete group"); }
  };

  // --- NEW: LEAVE GROUP ---
  const handleLeaveGroup = async () => {
      if(!window.confirm("Leave this group? You must have 0 balance.")) return;
      try {
          // Removes CURRENT USER from group
          await api.delete(`/groups/${groupId}/members/${currentUser.userId}`);
          onExitGroup();
          showAlert("Success", "You left the group", "success");
      } catch(e) {
          // SAFE ERROR HANDLING: Check if it's an object or string
          let msg = "Could not leave group";
          if (e.response && e.response.data) {
              msg = typeof e.response.data === 'object' ? (e.response.data.errorMessage || "Error") : e.response.data;
          }
          showAlert("Error", msg);
      }
  };

  // --- NEW: REMOVE OTHER MEMBER ---
  const handleRemoveMember = async (memberId) => {
      if(!window.confirm("Remove this member?")) return;
      try {
          await api.delete(`/groups/${groupId}/members/${memberId}`);
          setRefresh(!refresh);
          showAlert("Success", "Member removed", "success");
      } catch(e) { 
          let msg = "Failed to remove";
          if (e.response && e.response.data) {
              msg = typeof e.response.data === 'object' ? (e.response.data.errorMessage || "Error") : e.response.data;
          }
          showAlert("Error", msg); 
      }
  };

  if(!group || !balances) return <div style={{padding: 20}}>Loading...</div>;

  return (
    <>
      <div className="dashboard-grid">
        <div className="center-panel">
            <div className="header">
                <div>
                    <h2>{group.name}</h2>
                    {/* TOOLBAR: EXPORT / LEAVE / DELETE */}
                    <div style={{display:'flex', gap: 15, marginTop: 5, fontSize: '0.8rem', color: '#888'}}>
                        <span onClick={handleExport} style={{cursor:'pointer', display:'flex', alignItems:'center', gap:5}} className="hover-text">
                            <Download size={14}/> Export CSV
                        </span>
                        
                        {/* Leave Group Button */}
                        <span onClick={handleLeaveGroup} style={{cursor:'pointer', display:'flex', alignItems:'center', gap:5}} className="hover-text">
                            <LeaveIcon size={14}/> Leave Group
                        </span>

                        {/* Delete Group Button (Ideally checking if admin) */}
                        <span onClick={handleDeleteGroup} style={{cursor:'pointer', display:'flex', alignItems:'center', gap:5, color:'#ff652f'}} className="hover-text">
                            <Trash2 size={14}/> Delete Group
                        </span>
                    </div>
                </div>
                <div style={{display:'flex', gap: 10}}>
                    <button className="btn btn-primary" onClick={() => setShowAddExpense(true)}>Add Expense</button>
                    <button className="btn btn-dark" onClick={() => setShowAddMember(true)}>+ Member</button>
                </div>
            </div>
            
            <div style={{overflowY: 'auto', paddingBottom: 20}}>
                {history.length === 0 && (
                    <div style={{padding: 60, textAlign: 'center', color: '#666'}}>
                        <History size={48} style={{marginBottom: 20, opacity: 0.5}}/>
                        <p>No activity yet.</p>
                    </div>
                )}
                {history.map(log => {
                    let title = log.details;
                    let subtext = "";
                    let amount = "";
                    let expenseId = null;
                    let isExpense = log.action === 'EXPENSE';

                    if (isExpense && log.details.includes('|||')) {
                        const parts = log.details.split('|||');
                        title = parts[0]; amount = parts[1]; subtext = `${parts[2]} paid ₹${amount}`; expenseId = parts[3];
                    } else if (log.action === 'SETTLEMENT') {
                        title = log.details; subtext = "Payment"; 
                    }

                    return (
                        <div key={log.id} style={{borderBottom: '1px solid #333'}}>
                            <div className="feed-item" onClick={() => isExpense ? toggleExpand(log.id, expenseId) : null} style={{cursor: isExpense ? 'pointer' : 'default'}}>
                                <div style={{marginRight: 15, textAlign: 'center', color: '#888', fontSize: '0.7rem', textTransform: 'uppercase', width: 35}}>
                                    <div>{new Date(log.timestamp).toLocaleString('default', { month: 'short' })}</div>
                                    <div style={{fontSize: '1.2rem', fontWeight: 'bold', color: '#ccc'}}>{new Date(log.timestamp).getDate()}</div>
                                </div>
                                <div className="feed-icon" style={{background: isExpense ? '#2c2c2c' : 'rgba(28, 194, 159, 0.1)'}}>
                                    {isExpense ? <Receipt size={18} color="#ff652f"/> : <CheckCircle size={18} color="#1cc29f"/>}
                                </div>
                                <div style={{flex: 1}}>
                                    <div style={{fontSize: '1rem', fontWeight: isExpense ? 'bold' : 'normal', color: isExpense ? '#fff' : '#ccc'}}>{title}</div>
                                    <div style={{fontSize: '0.8rem', color: !isExpense ? '#1cc29f' : '#888'}}>{subtext}</div>
                                </div>
                                {isExpense && <div style={{textAlign: 'right'}}><div style={{fontSize: '0.7rem', color: '#888'}}>total</div><div style={{fontWeight: 'bold', fontSize: '0.9rem'}}>₹{amount}</div></div>}
                            </div>
                            {expandedId === log.id && isExpense && (
                                <div style={{background: '#1a1a1a', padding: '15px 15px 15px 70px', borderTop: '1px dashed #333'}}>
                                    {!expandedData ? <div style={{color: '#666'}}>Loading...</div> : (
                                        <div>
                                            {expandedData.shares.map(share => (
                                                <div key={share.userId} style={{display:'flex', justifyContent:'space-between', fontSize: '0.85rem', marginBottom: 5, color: '#aaa'}}>
                                                    <span>{share.userName} owes</span>
                                                    <span style={{color: '#ff652f'}}>₹{share.amount.toFixed(2)}</span>
                                                </div>
                                            ))}
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>
                    );
                })}
            </div>
        </div>

        <div className="right-panel">
            <div className="section-label" style={{marginBottom: 15}}>Group Balances</div>
            {balances.balances.map(b => (
                <div key={b.userId} className="balance-row">
                    <div className="user-avatar">{b.name.charAt(0)}</div>
                    <div style={{flex: 1}}>
                        <div>{b.name}</div>
                        {b.netBalance > 0 && <small className="green">gets back ₹{b.netBalance.toFixed(2)}</small>}
                        {b.netBalance < 0 && <small className="red">owes ₹{Math.abs(b.netBalance).toFixed(2)}</small>}
                        {b.netBalance === 0 && <small style={{color:'#666'}}>settled</small>}
                    </div>
                    {/* Remove Member Icon (Only shows for other users) */}
                    {b.userId !== currentUser.userId && (
                        <div onClick={() => handleRemoveMember(b.userId)} style={{cursor:'pointer', opacity: 0.5}} title="Remove Member">
                            <UserMinus size={14} color="#ff652f"/>
                        </div>
                    )}
                </div>
            ))}
            <div className="section-label" style={{marginTop: 30, marginBottom: 15}}>Suggested Payments</div>
            {balances.settlements.length === 0 ? <p style={{color:'#666', fontSize:'0.9rem'}}>All settled up!</p> :
                balances.settlements.map((s, i) => (
                    <div key={i} style={{background: '#252525', padding: 12, borderRadius: 6, marginBottom: 10, border: '1px solid #333'}}>
                         <div style={{fontSize: '0.85rem', marginBottom: 8, color: '#ccc'}}>
                            {s.fromUser === currentUser.userId ? 
                               <span>You owe <b>{getUserName(s.toUser)}</b></span> : 
                               <span><b>{getUserName(s.fromUser)}</b> owes <b>{getUserName(s.toUser)}</b></span>
                            }
                         </div>
                         <div style={{display:'flex', justifyContent:'space-between', alignItems:'center'}}>
                            <strong style={{fontSize:'1.1rem'}}>₹{s.amount}</strong>
                            {s.fromUser === currentUser.userId && <button className="btn btn-secondary" style={{padding: '6px 12px', fontSize: '0.8rem'}} onClick={() => openSettleModal(s.toUser, s.amount)}>Pay</button>}
                         </div>
                    </div>
                ))
            }
        </div>
      </div>

      {showAddExpense && <AddExpenseModal groupId={groupId} members={group.members} currentUser={currentUser} onClose={()=>setShowAddExpense(false)} onSuccess={()=>{setShowAddExpense(false); setRefresh(!refresh); showAlert("Success", "Expense added!", "success")}} showAlert={showAlert} />}
      
      {showAddMember && <InputModal title="Add Member" placeholder="Enter email address" onClose={()=>setShowAddMember(false)} onSubmit={addMember} />}

      {settleData && <SettleModal settleData={settleData} currentUser={currentUser} onClose={() => setSettleData(null)} onSuccess={() => { setSettleData(null); setRefresh(!refresh); showAlert("Success", "Payment recorded!", "success") }} showAlert={showAlert} />}
    </>
  );
}

// --- REUSABLE COMPONENTS ---

function InputModal({ title, placeholder, onClose, onSubmit }) {
    const [val, setVal] = useState('');
    return (
        <div className="modal-overlay">
            <div className="modal">
                <div className="modal-header"><span>{title}</span><span onClick={onClose} style={{cursor:'pointer'}}><X size={20}/></span></div>
                <div className="modal-body">
                    <form onSubmit={(e) => { e.preventDefault(); onSubmit(val); }}>
                        <input className="input-dark" autoFocus placeholder={placeholder} value={val} onChange={e=>setVal(e.target.value)} required />
                        <button className="btn btn-secondary" style={{width:'100%'}}>Confirm</button>
                    </form>
                </div>
            </div>
        </div>
    );
}

function AlertModal({ info, onClose }) {
    return (
        <div className="modal-overlay" style={{zIndex: 200}}>
            <div className="modal" style={{width: 350, textAlign: 'center'}}>
                <div className="modal-body">
                    <div style={{marginBottom: 15, display:'flex', justifyContent:'center'}}>
                        {info.type === 'error' ? <AlertCircle size={40} color="#ff652f" /> : <CheckCircle size={40} color="#1cc29f" />}
                    </div>
                    <h3 style={{color: 'white', marginBottom: 10}}>{info.title}</h3>
                    <p style={{color: '#aaa', marginBottom: 20}}>{info.msg}</p>
                    <button className="btn btn-secondary" onClick={onClose} style={{width:'100%'}}>Okay</button>
                </div>
            </div>
        </div>
    );
}

function SettleModal({ settleData, currentUser, onClose, onSuccess, showAlert }) {
    const [amount, setAmount] = useState(settleData.amount);
    const handleSubmit = async (e) => {
        e.preventDefault();
        const num = parseFloat(amount);
        if (isNaN(num) || num <= 0) { showAlert("Error", "Invalid amount"); return; }
        try {
            await api.post('/settlements', { groupId: settleData.groupId, fromUser: currentUser.userId, toUser: settleData.toUser, amount: num });
            onSuccess();
        } catch (err) { showAlert("Error", "Settlement failed"); }
    };
    return (
        <div className="modal-overlay">
            <div className="modal">
                <div className="modal-header"><span>Record Payment</span><span onClick={onClose} style={{cursor:'pointer'}}><X size={20}/></span></div>
                <div className="modal-body">
                    <form onSubmit={handleSubmit}>
                        <div style={{textAlign:'center', marginBottom:20, color:'#ccc'}}>PAYING <b style={{color:'white'}}>{settleData.toName}</b></div>
                        <div style={{display:'flex', justifyContent:'center', gap:10, marginBottom:20}}>
                            <IndianRupee size={28} color="#1cc29f"/>
                            <input className="input-dark" type="number" value={amount} onChange={e=>setAmount(e.target.value)} style={{fontSize:'2rem', width:150, textAlign:'center', margin:0}} autoFocus />
                        </div>
                        <button className="btn btn-secondary" style={{width:'100%'}}>Pay ₹{amount}</button>
                    </form>
                </div>
            </div>
        </div>
    );
}

// --- ADD EXPENSE MODAL (With Participant Selection) ---
function AddExpenseModal({ groupId, members, currentUser, onClose, onSuccess, showAlert }) {
    const [title, setTitle] = useState('');
    const [amount, setAmount] = useState('');
    const [splitType, setSplitType] = useState('EQUAL'); 
    
    // NEW: Track who is involved in this expense
    const [selectedIds, setSelectedIds] = useState([]);
    
    const [exactShares, setExactShares] = useState({});

    // Initialize: Select Everyone by default
    useEffect(() => {
        const initialIds = members.map(m => m.id);
        setSelectedIds(initialIds);
        
        const initialShares = {}; 
        members.forEach(m => initialShares[m.id] = 0);
        setExactShares(initialShares);
    }, [members]);

    const toggleParticipant = (id) => {
        if (selectedIds.includes(id)) {
            // Prevent unchecking everyone (at least 1 person needed)
            if (selectedIds.length === 1) {
                showAlert("Error", "At least one person must be involved.");
                return;
            }
            setSelectedIds(selectedIds.filter(pid => pid !== id));
        } else {
            setSelectedIds([...selectedIds, id]);
        }
    };

    const handleExactChange = (id, val) => setExactShares({ ...exactShares, [id]: parseFloat(val) || 0 });

    const handleSubmit = async (e) => {
        e.preventDefault();
        const numAmount = parseFloat(amount);
        
        if(isNaN(numAmount) || numAmount <= 0) { showAlert("Error", "Invalid amount"); return; }
        if(selectedIds.length === 0) { showAlert("Error", "Select at least one participant"); return; }

        let exactList = null;
        
        if (splitType === 'EXACT') {
            // Validate that shares sum up to total
            // Only sum up the SELECTED participants' shares
            const sum = selectedIds.reduce((acc, id) => acc + (exactShares[id] || 0), 0);
            
            if (Math.abs(sum - numAmount) > 0.1) { 
                showAlert("Math Error", `Selected shares total ₹${sum}, but expense is ₹${numAmount}`); 
                return; 
            }
            
            // Map shares specifically for the backend List<Double>
            // The backend expects the values to correspond to the participants list order
            exactList = selectedIds.map(id => exactShares[id] || 0);
        }

        try { 
            await api.post('/expenses', { 
                groupId, 
                title, 
                amount: numAmount, 
                paidBy: currentUser.userId, 
                splitType, 
                participants: selectedIds, // Send only selected IDs
                exactAmounts: exactList 
            }); 
            onSuccess(); 
        } catch (err) { showAlert("Error", "Failed to save expense"); }
    };

    return (
        <div className="modal-overlay">
            <div className="modal">
                <div className="modal-header"><span>Add Expense</span><span onClick={onClose} style={{cursor:'pointer'}}><X size={20}/></span></div>
                <div className="modal-body">
                    <form onSubmit={handleSubmit}>
                        <input className="input-dark" placeholder="Description" value={title} onChange={e=>setTitle(e.target.value)} autoFocus required/>
                        <div style={{display:'flex', alignItems:'center', gap: 10, marginBottom: 10}}>
                            <IndianRupee size={20} color="#1cc29f"/>
                            <input className="input-dark" type="number" placeholder="0.00" value={amount} onChange={e=>setAmount(e.target.value)} style={{fontSize: '1.2rem'}} required />
                        </div>

                        {/* --- NEW: PARTICIPANT SELECTION --- */}
                        <div className="participants-section">
                            <div className="participants-header">Split with ({selectedIds.length} selected)</div>
                            <div style={{maxHeight: '100px', overflowY: 'auto'}}>
                                {members.map(m => (
                                    <div key={m.id} className="participant-row" onClick={() => toggleParticipant(m.id)}>
                                        <input 
                                            type="checkbox" 
                                            className="checkbox"
                                            checked={selectedIds.includes(m.id)}
                                            readOnly 
                                        />
                                        <span style={{color: selectedIds.includes(m.id) ? 'white' : '#666'}}>
                                            {m.name} {m.id === currentUser.userId ? '(You)' : ''}
                                        </span>
                                    </div>
                                ))}
                            </div>
                        </div>

                        <div className="split-tabs">
                            <div className={`split-tab ${splitType === 'EQUAL' ? 'active' : ''}`} onClick={() => setSplitType('EQUAL')}>= Equal</div>
                            <div className={`split-tab ${splitType === 'EXACT' ? 'active' : ''}`} onClick={() => setSplitType('EXACT')}>1.23 Exact</div>
                        </div>

                        {/* EXACT SPLIT INPUTS (Only show for Selected Users) */}
                        {splitType === 'EXACT' && (
                            <div style={{maxHeight: 150, overflowY: 'auto', marginBottom: 20, border: '1px solid #333', borderRadius: 8}}>
                                {members.filter(m => selectedIds.includes(m.id)).map(m => (
                                    <div key={m.id} className="member-split-row">
                                        <span>{m.name}</span>
                                        <div style={{display:'flex', gap:5}}>
                                            ₹ <input className="split-input" type="number" value={exactShares[m.id]} onChange={e => handleExactChange(m.id, e.target.value)} />
                                        </div>
                                    </div>
                                ))}
                                <div style={{textAlign: 'right', padding: 10, color: '#888', fontSize: '0.8rem'}}>
                                    Unassigned: ₹{(parseFloat(amount || 0) - selectedIds.reduce((acc, id) => acc + (exactShares[id] || 0), 0)).toFixed(2)}
                                </div>
                            </div>
                        )}
                        <button className="btn btn-secondary" style={{width:'100%'}}>Save</button>
                    </form>
                </div>
            </div>
        </div>
    );
}

// 5. AUTH PAGE WITH SAFE ERROR HANDLING
function AuthPage({ onLogin, showAlert }) {
    const [isRegister, setIsRegister] = useState(false);
    const [formData, setFormData] = useState({ email: '', password: '', name: '', phoneNumber: '' });
  
    const handleSubmit = async (e) => {
      e.preventDefault();
      const endpoint = isRegister ? '/users/register' : '/users/login';
      try {
        const res = await api.post(endpoint, formData);
        if(isRegister) { showAlert("Success", "Account created! Please Login.", "success"); setIsRegister(false); }
        else onLogin(res.data.token, { userId: res.data.userId, name: res.data.name });
      } catch(e) { 
          // FIX: Handle both string and object error responses safely
          let msg = "Authentication failed";
          if (e.response && e.response.data) {
              msg = typeof e.response.data === 'object' ? (e.response.data.errorMessage || "Check credentials") : e.response.data;
          }
          showAlert("Error", msg); 
      }
    };
  
    return (
      <div className="auth-page">
        <div className="auth-card">
            <div style={{display:'flex', justifyContent:'center', marginBottom: 15}}><PieChart size={48} color="#1cc29f" /></div>
            <h1 className="auth-title">FairShare</h1>
            <form onSubmit={handleSubmit}>
                {isRegister && <><input className="auth-input" placeholder="Full Name" onChange={e=>setFormData({...formData, name: e.target.value})} required /><input className="auth-input" placeholder="Phone" onChange={e=>setFormData({...formData, phoneNumber: e.target.value})} /></>}
                <input className="auth-input" placeholder="Email" onChange={e=>setFormData({...formData, email: e.target.value})} required />
                <input className="auth-input" placeholder="Password" type="password" onChange={e=>setFormData({...formData, password: e.target.value})} required />
                <button className="auth-btn">{isRegister ? 'Sign Up' : 'Log In'}</button>
            </form>
            <p className="auth-toggle" onClick={()=>setIsRegister(!isRegister)}>{isRegister ? 'Already have an account? Log in' : 'New? Sign up'}</p>
        </div>
      </div>
    );
}

export default App;