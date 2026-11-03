(() => {
  const screens = [...document.querySelectorAll('.screen')];
  const go = (name) => {
    screens.forEach(s => s.classList.toggle('active', s.dataset.screen === name));
    window.scrollTo({ top: 0, behavior: 'instant' });
    history.pushState({ screen: name }, '', `#${name}`);
  };

  const toast = (message) => {
    if (window.Android && typeof window.Android.showToast === 'function') {
      Android.showToast(message);
    } else {
      alert(message);
    }
  };

  const loginButton = document.getElementById('loginButton');
  const signupButton = document.getElementById('signupButton');
  const authMessage = document.getElementById('authMessage');
  const signupMessage = document.getElementById('signupMessage');
  const saveAnimalButton = document.getElementById('saveAnimal');
  const animalMessage = document.getElementById('animalMessage');
  const animalList = document.getElementById('animalList');
  const saveCaseButton = document.getElementById('saveCase');
  const caseMessage = document.getElementById('caseMessage');
  const caseList = document.getElementById('caseList');
  const logoutButton = document.getElementById('logoutButton');
  const vetLogoutButton = document.getElementById('vetLogoutButton');
  const adminLogoutButton = document.getElementById('adminLogoutButton');
  const vetCaseList = document.getElementById('vetCaseList');
  const adminMetrics = document.getElementById('adminMetrics');
  const adminRecentList = document.getElementById('adminRecentList');
  const reportTitle = document.getElementById('reportTitle');
  const reportHeader = document.getElementById('reportHeader');
  const reportLead = document.getElementById('reportLead');
  const protectedIdentityNotice = document.getElementById('protectedIdentityNotice');
  let lastAuthAction = 'login';
  let currentReportType = 'lost';

  const setMessage = (element, message, type = '') => {
    if (!element) return;
    element.textContent = message || '';
    element.className = `form-message${type ? ` ${type}` : ''}`;
  };

  const setAuthBusy = (busy, action = lastAuthAction) => {
    lastAuthAction = action || lastAuthAction;
    if (loginButton) {
      loginButton.disabled = busy;
      loginButton.classList.toggle('loading', busy && lastAuthAction === 'login');
      loginButton.textContent = busy && lastAuthAction === 'login' ? 'Ingresando…' : 'Iniciar Sesión';
    }
    if (signupButton) {
      signupButton.disabled = busy;
      signupButton.classList.toggle('loading', busy && lastAuthAction === 'register');
      signupButton.textContent = busy && lastAuthAction === 'register' ? 'Creando cuenta…' : 'Crear cuenta';
    }
  };

  const setAnimalBusy = (busy) => {
    if (!saveAnimalButton) return;
    saveAnimalButton.disabled = busy;
    saveAnimalButton.classList.toggle('loading', busy);
    saveAnimalButton.textContent = busy ? 'Guardando…' : 'Guardar animal';
  };

  const requestMyAnimals = () => {
    if (window.Android && typeof window.Android.loadMyAnimals === 'function') {
      Android.loadMyAnimals();
    }
  };

  const setCaseBusy = (busy) => {
    if (!saveCaseButton) return;
    saveCaseButton.disabled = busy;
    saveCaseButton.classList.toggle('loading', busy);
    saveCaseButton.textContent = busy ? 'Enviando…' : 'Enviar reporte';
  };

  const requestMyCases = () => {
    if (window.Android && typeof window.Android.loadMyCases === 'function') {
      Android.loadMyCases();
    }
  };

  const requestVeterinarianCases = () => {
    if (window.Android && typeof window.Android.loadAssignedCases === 'function') {
      Android.loadAssignedCases();
    }
  };

  const requestAdminDashboard = () => {
    if (window.Android && typeof window.Android.loadAdminDashboard === 'function') {
      Android.loadAdminDashboard();
    }
  };

  const openPortalForRole = (data) => {
    const role = data && data.activeRole ? data.activeRole : '';
    const displayName = data && data.name ? data.name : (data && data.email ? data.email : 'Usuario');

    if (role === 'citizen') {
      const signedInUser = document.getElementById('signedInUser');
      if (signedInUser) signedInUser.textContent = `Sesión: ${displayName}`;
      go('home');
      requestMyAnimals();
      requestMyCases();
      return;
    }

    if (role === 'veterinarian') {
      const vetSignedInUser = document.getElementById('vetSignedInUser');
      if (vetSignedInUser) vetSignedInUser.textContent = `Sesión: ${displayName}`;
      go('vet-home');
      requestVeterinarianCases();
      return;
    }

    if (role === 'administrator') {
      const adminSignedInUser = document.getElementById('adminSignedInUser');
      if (adminSignedInUser) adminSignedInUser.textContent = `Sesión: ${displayName}`;
      go('admin-home');
      requestAdminDashboard();
      return;
    }

    setMessage(authMessage, 'El rol de esta cuenta no está habilitado.', 'error');
    if (window.Android && typeof window.Android.logout === 'function') Android.logout();
  };

  const reportLabels = {
    lost: { title: 'Reportar pérdida', lead: 'Registra dónde y cuándo identificaste la pérdida. El caso quedará recibido para seguimiento.' },
    found: { title: 'Reportar hallazgo', lead: 'Registra el animal encontrado y su ubicación para facilitar la búsqueda de su responsable.' },
    abuse: { title: 'Reportar presunto maltrato', lead: 'Describe la situación observada. Este reporte usa identidad protegida para el denunciante.' }
  };

  const configureReport = (type) => {
    currentReportType = reportLabels[type] ? type : 'lost';
    const label = reportLabels[currentReportType];
    if (reportTitle) reportTitle.textContent = label.title;
    if (reportHeader) reportHeader.textContent = label.title;
    if (reportLead) reportLead.textContent = label.lead;
    if (protectedIdentityNotice) protectedIdentityNotice.hidden = currentReportType !== 'abuse';
    setMessage(caseMessage, '');
    go('report');
  };

  const typeLabel = (type) => ({ lost: 'Pérdida', found: 'Hallazgo', abuse: 'Presunto maltrato' }[type] || 'Reporte');
  const priorityLabel = (priority) => ({ low: 'Baja', medium: 'Media', high: 'Alta', critical: 'Crítica', pending_review: 'Pendiente de revisión' }[priority] || priority || 'Pendiente');
  const statusLabel = (status) => ({ received: 'Recibido', under_review: 'En revisión', assigned: 'Asignado', in_progress: 'En atención', completed: 'Realizado' }[status] || status || 'Recibido');

  const renderCaseList = (cases) => {
    if (!caseList) return;
    while (caseList.firstChild) caseList.removeChild(caseList.firstChild);

    if (!Array.isArray(cases) || cases.length === 0) {
      const empty = document.createElement('div');
      empty.className = 'empty-state';
      const title = document.createElement('strong');
      title.textContent = 'Aún no has enviado reportes';
      const detail = document.createElement('span');
      detail.textContent = 'Cuando envíes uno podrás consultar aquí su estado general.';
      empty.append(title, detail);
      caseList.appendChild(empty);
      return;
    }

    const grid = document.createElement('div');
    grid.className = 'case-grid';
    cases.forEach(item => {
      const card = document.createElement('article');
      card.className = 'case-card';

      const top = document.createElement('div');
      top.className = 'case-card-top';
      const name = document.createElement('strong');
      name.textContent = typeLabel(item.type);
      const priority = document.createElement('em');
      priority.className = item.finalPriority === 'critical' || item.finalPriority === 'high' ? 'status review' : 'status ok';
      priority.textContent = priorityLabel(item.finalPriority);
      top.append(name, priority);

      const location = document.createElement('span');
      location.textContent = item.neighborhoodOrVillage || 'Zipaquirá';
      const status = document.createElement('b');
      status.textContent = `Estado: ${statusLabel(item.status)}`;
      card.append(top, location, status);
      grid.appendChild(card);
    });
    caseList.appendChild(grid);
  };


  const renderVeterinarianCases = (cases) => {
    if (!vetCaseList) return;
    while (vetCaseList.firstChild) vetCaseList.removeChild(vetCaseList.firstChild);

    if (!Array.isArray(cases) || cases.length === 0) {
      const empty = document.createElement('div');
      empty.className = 'empty-state';
      const title = document.createElement('strong');
      title.textContent = 'No tienes casos asignados';
      const detail = document.createElement('span');
      detail.textContent = 'Cuando se asigne un caso a tu cuenta aparecerá aquí.';
      empty.append(title, detail);
      vetCaseList.appendChild(empty);
      return;
    }

    const grid = document.createElement('div');
    grid.className = 'case-grid';
    cases.forEach(item => {
      const card = document.createElement('article');
      card.className = 'case-card';

      const top = document.createElement('div');
      top.className = 'case-card-top';
      const name = document.createElement('strong');
      name.textContent = typeLabel(item.type);
      const priority = document.createElement('em');
      priority.className = item.finalPriority === 'critical' || item.finalPriority === 'high' ? 'status review' : 'status ok';
      priority.textContent = priorityLabel(item.finalPriority);
      top.append(name, priority);

      const location = document.createElement('span');
      location.textContent = [item.neighborhoodOrVillage, item.addressOrLocation].filter(Boolean).join(' · ') || 'Zipaquirá';

      const condition = document.createElement('span');
      condition.textContent = item.animalCondition ? `Condición: ${item.animalCondition}` : 'Condición no registrada';

      const status = document.createElement('b');
      status.textContent = `Estado: ${statusLabel(item.status)}`;

      card.append(top, location, condition, status);

      const nextStatus = item.status === 'assigned'
        ? 'in_progress'
        : (item.status === 'in_progress' ? 'completed' : '');

      if (nextStatus) {
        const action = document.createElement('button');
        action.className = 'btn secondary block';
        action.dataset.vetCaseId = item.id;
        action.dataset.vetNextStatus = nextStatus;
        action.textContent = nextStatus === 'in_progress' ? 'Iniciar atención' : 'Marcar como realizado';
        card.appendChild(action);
      }

      grid.appendChild(card);
    });

    vetCaseList.appendChild(grid);
  };


  const renderAdminDashboard = (data) => {
    if (adminMetrics) {
      const cards = adminMetrics.querySelectorAll('.metric-card strong');
      if (cards[0]) cards[0].textContent = String(data.userCount || 0);
      if (cards[1]) cards[1].textContent = String(data.animalCount || 0);
      if (cards[2]) cards[2].textContent = String(data.caseCount || 0);
    }

    if (!adminRecentList) return;
    while (adminRecentList.firstChild) adminRecentList.removeChild(adminRecentList.firstChild);

    const sections = [
      { title: 'Usuarios', items: data.users || [], describe: item => [item.name, item.activeRole, item.status].filter(Boolean).join(' · ') },
      { title: 'Animales', items: data.animals || [], describe: item => [item.name, item.species, item.status].filter(Boolean).join(' · ') },
      { title: 'Casos', items: data.cases || [], describe: item => [typeLabel(item.type), statusLabel(item.status), item.neighborhoodOrVillage].filter(Boolean).join(' · ') }
    ];

    sections.forEach(section => {
      const heading = document.createElement('strong');
      heading.textContent = section.title;
      adminRecentList.appendChild(heading);

      if (!section.items.length) {
        const empty = document.createElement('span');
        empty.textContent = 'Sin registros para mostrar.';
        adminRecentList.appendChild(empty);
        return;
      }

      section.items.forEach(item => {
        const row = document.createElement('span');
        row.textContent = section.describe(item);
        adminRecentList.appendChild(row);
      });
    });
  };

  const resetCaseForm = () => {
    ['reportDescription', 'reportNeighborhoodOrVillage', 'reportAddressOrLocation', 'reportReferencePoint', 'reportSuspectedResponsible'].forEach(id => {
      const input = document.getElementById(id);
      if (input) input.value = '';
    });
    const count = document.getElementById('reportAnimalCount');
    const priority = document.getElementById('reportPriority');
    const condition = document.getElementById('reportAnimalCondition');
    const known = document.getElementById('reportResponsibleKnown');
    if (count) count.value = '1';
    if (priority) priority.value = 'system';
    if (condition) condition.value = '';
    if (known) known.value = 'unknown';
  };

  const renderAnimalList = (animals) => {
    if (!animalList) return;
    while (animalList.firstChild) animalList.removeChild(animalList.firstChild);

    if (!Array.isArray(animals) || animals.length === 0) {
      const empty = document.createElement('div');
      empty.className = 'empty-state';
      const title = document.createElement('strong');
      title.textContent = 'Aún no tienes animales registrados';
      const detail = document.createElement('span');
      detail.textContent = 'Agrega tu primer animal para verlo aquí.';
      empty.append(title, detail);
      animalList.appendChild(empty);
      return;
    }

    const grid = document.createElement('div');
    grid.className = 'animal-grid';
    animals.forEach(animal => {
      const card = document.createElement('article');
      card.className = 'animal-card';

      const art = document.createElement('div');
      art.className = 'animal-art';
      art.textContent = animal.species || 'Animal';

      const body = document.createElement('div');
      const name = document.createElement('strong');
      name.textContent = animal.name || 'Sin nombre';
      const description = document.createElement('span');
      description.textContent = [animal.species, animal.breed].filter(Boolean).join(' · ') || 'Animal registrado';
      const status = document.createElement('em');
      status.className = 'status ok';
      status.textContent = animal.status === 'active' ? 'Activo' : (animal.status || 'Registrado');

      body.append(name, description, status);
      card.append(art, body);
      grid.appendChild(card);
    });
    animalList.appendChild(grid);
  };

  const resetAnimalForm = () => {
    const name = document.getElementById('animalName');
    const breed = document.getElementById('animalBreed');
    const color = document.getElementById('animalColor');
    const age = document.getElementById('animalAge');
    const neighborhood = document.getElementById('animalNeighborhoodOrVillage');
    const address = document.getElementById('animalAddressOrLocation');
    const reference = document.getElementById('animalReferencePoint');
    [name, breed, color, age, neighborhood, address, reference].forEach(input => {
      if (input) input.value = '';
    });
  };

  window.BAZNative = {
    onNativeEvent(eventName, payload) {
      const data = payload || {};
      if (eventName === 'auth:loading') {
        const action = data.action === 'register' ? 'register' : 'login';
        setAuthBusy(true, action);
        setMessage(action === 'register' ? signupMessage : authMessage, 'Conectando con el servicio…');
        return;
      }

      if (eventName === 'auth:error') {
        setAuthBusy(false);
        setMessage(lastAuthAction === 'register' ? signupMessage : authMessage, data.message || 'No fue posible completar la operación.', 'error');
        return;
      }

      if (eventName === 'auth:signedout') {
        const signedInUser = document.getElementById('signedInUser');
        if (signedInUser) signedInUser.textContent = 'Sesión cerrada.';
        toast('Sesión cerrada.');
        go('login');
        return;
      }

      if (eventName === 'auth:success' || eventName === 'auth:registered') {
        setAuthBusy(false);
        setMessage(authMessage, '');
        setMessage(signupMessage, '');
        if (eventName === 'auth:registered') toast('Cuenta ciudadana creada correctamente.');
        openPortalForRole(data);
        return;
      }

      if (eventName === 'animals:loading') {
        if (data.action === 'save') {
          setAnimalBusy(true);
          setMessage(animalMessage, 'Guardando el animal…');
        }
        return;
      }

      if (eventName === 'animals:error') {
        setAnimalBusy(false);
        setMessage(animalMessage, data.message || 'No fue posible completar la operación con el animal.', 'error');
        toast(data.message || 'No fue posible completar la operación con el animal.');
        return;
      }

      if (eventName === 'animals:saved') {
        setAnimalBusy(false);
        setMessage(animalMessage, 'Animal registrado correctamente.', 'success');
        toast('Animal registrado correctamente.');
        resetAnimalForm();
        go('home');
        requestMyAnimals();
        return;
      }

      if (eventName === 'animals:list') {
        renderAnimalList(data.animals || []);
      }


      if (eventName === 'cases:loading') {
        if (data.action === 'save') {
          setCaseBusy(true);
          setMessage(caseMessage, 'Enviando el reporte…');
        }
        return;
      }

      if (eventName === 'cases:error') {
        setCaseBusy(false);
        setMessage(caseMessage, data.message || 'No fue posible completar el reporte.', 'error');
        toast(data.message || 'No fue posible completar el reporte.');
        return;
      }

      if (eventName === 'cases:saved') {
        setCaseBusy(false);
        setMessage(caseMessage, 'Reporte recibido correctamente.', 'success');
        toast('Reporte recibido correctamente.');
        resetCaseForm();
        go('home');
        requestMyCases();
        return;
      }

      if (eventName === 'cases:list') {
        renderCaseList(data.cases || []);
        return;
      }

      if (eventName === 'vet:cases') {
        renderVeterinarianCases(data.cases || []);
        return;
      }

      if (eventName === 'vet:case-updated') {
        toast('Estado del caso actualizado.');
        requestVeterinarianCases();
        return;
      }

      if (eventName === 'vet:error') {
        toast(data.message || 'No fue posible completar la operación veterinaria.');
        return;
      }

      if (eventName === 'admin:dashboard') {
        renderAdminDashboard(data);
        return;
      }

      if (eventName === 'admin:error') {
        toast(data.message || 'No fue posible cargar el panel administrativo.');
        return;
      }
    }
  };

  document.addEventListener('click', (event) => {
    const nav = event.target.closest('[data-go]');
    if (nav) {
      event.preventDefault();
      go(nav.dataset.go);
      return;
    }

    const reportAction = event.target.closest('[data-report-type]');
    if (reportAction) {
      event.preventDefault();
      configureReport(reportAction.dataset.reportType);
      return;
    }

    const vetAction = event.target.closest('[data-vet-case-id]');
    if (vetAction) {
      event.preventDefault();
      if (window.Android && typeof window.Android.updateAssignedCaseStatus === 'function') {
        Android.updateAssignedCaseStatus(vetAction.dataset.vetCaseId, vetAction.dataset.vetNextStatus);
      }
    }
  });

  if (loginButton) loginButton.addEventListener('click', () => {
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    setMessage(authMessage, '');
    lastAuthAction = 'login';

    if (!window.Android || typeof window.Android.login !== 'function') {
      setMessage(authMessage, 'El servicio de autenticación requiere ejecutar la app Android.', 'error');
      return;
    }
    Android.login(email, password);
  });

  if (signupButton) signupButton.addEventListener('click', () => {
    const name = document.getElementById('signupName').value.trim();
    const email = document.getElementById('signupEmail').value.trim();
    const password = document.getElementById('signupPassword').value;
    setMessage(signupMessage, '');
    lastAuthAction = 'register';

    if (!window.Android || typeof window.Android.registerCitizen !== 'function') {
      setMessage(signupMessage, 'El servicio de registro requiere ejecutar la app Android.', 'error');
      return;
    }
    Android.registerCitizen(name, email, password);
  });

  document.querySelectorAll('.species').forEach(button => {
    button.addEventListener('click', () => {
      document.querySelectorAll('.species').forEach(b => b.classList.remove('selected'));
      button.classList.add('selected');
    });
  });

  document.querySelectorAll('.direction').forEach(button => {
    button.addEventListener('click', () => {
      document.querySelectorAll('.direction').forEach(b => b.classList.remove('selected'));
      button.classList.add('selected');
    });
  });

  document.querySelectorAll('[data-sterilized]').forEach(button => {
    button.addEventListener('click', () => {
      document.querySelectorAll('[data-sterilized]').forEach(b => b.classList.remove('selected-choice'));
      button.classList.add('selected-choice');
    });
  });

  if (saveAnimalButton) saveAnimalButton.addEventListener('click', () => {
    setMessage(animalMessage, '');
    const selectedSpecies = document.querySelector('.species.selected');
    const selectedDirection = document.querySelector('.direction.selected');
    const selectedSterilized = document.querySelector('[data-sterilized].selected-choice');

    const payload = {
      name: document.getElementById('animalName').value.trim(),
      species: selectedSpecies ? selectedSpecies.dataset.species : '',
      breed: document.getElementById('animalBreed').value.trim(),
      color: document.getElementById('animalColor').value.trim(),
      sex: document.getElementById('animalSex').value,
      approximateAge: document.getElementById('animalAge').value.trim(),
      size: document.getElementById('animalSize').value,
      sterilized: selectedSterilized ? selectedSterilized.dataset.sterilized === 'true' : false,
      addressType: selectedDirection ? selectedDirection.dataset.direction : '',
      municipality: document.getElementById('animalMunicipality').value.trim(),
      neighborhoodOrVillage: document.getElementById('animalNeighborhoodOrVillage').value.trim(),
      addressOrLocation: document.getElementById('animalAddressOrLocation').value.trim(),
      referencePoint: document.getElementById('animalReferencePoint').value.trim()
    };

    if (!window.Android || typeof window.Android.saveAnimal !== 'function') {
      setMessage(animalMessage, 'El registro de animales requiere ejecutar la app Android.', 'error');
      return;
    }
    Android.saveAnimal(JSON.stringify(payload));
  });


  if (saveCaseButton) saveCaseButton.addEventListener('click', () => {
    setMessage(caseMessage, '');
    const payload = {
      type: currentReportType,
      description: document.getElementById('reportDescription').value.trim(),
      userPriority: document.getElementById('reportPriority').value,
      animalCount: Number(document.getElementById('reportAnimalCount').value || 1),
      animalCondition: document.getElementById('reportAnimalCondition').value,
      municipality: document.getElementById('reportMunicipality').value.trim(),
      neighborhoodOrVillage: document.getElementById('reportNeighborhoodOrVillage').value.trim(),
      addressOrLocation: document.getElementById('reportAddressOrLocation').value.trim(),
      referencePoint: document.getElementById('reportReferencePoint').value.trim(),
      responsibleKnown: document.getElementById('reportResponsibleKnown').value,
      suspectedResponsible: document.getElementById('reportSuspectedResponsible').value.trim()
    };

    if (!window.Android || typeof window.Android.saveCase !== 'function') {
      setMessage(caseMessage, 'El envío de reportes requiere ejecutar la app Android.', 'error');
      return;
    }
    Android.saveCase(JSON.stringify(payload));
  });




  const logout = () => {
    if (window.Android && typeof window.Android.logout === 'function') {
      Android.logout();
    } else {
      toast('Cerrar sesión requiere ejecutar la app Android.');
    }
  };

  if (logoutButton) logoutButton.addEventListener('click', logout);
  if (vetLogoutButton) vetLogoutButton.addEventListener('click', logout);
  if (adminLogoutButton) adminLogoutButton.addEventListener('click', logout);


  window.addEventListener('popstate', (event) => {
    const requested = (event.state && event.state.screen) || location.hash.replace('#', '') || 'splash';
    screens.forEach(s => s.classList.toggle('active', s.dataset.screen === requested));
  });

  const initial = location.hash.replace('#', '') || 'splash';
  screens.forEach(s => s.classList.toggle('active', s.dataset.screen === initial));
})();
