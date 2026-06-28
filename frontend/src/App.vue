<template>
  <div id="app">
    <Navbar />
    <router-view />
  </div>
</template>

<script setup>
import Navbar from './components/Navbar.vue'
</script>

<style>
#app {
  min-height: 100vh;
  background-color: #f5f5f5;
}
</style>
